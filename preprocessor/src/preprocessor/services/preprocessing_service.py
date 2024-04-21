import logging
import os
from dataclasses import dataclass
from uuid import UUID, uuid4

from PIL import Image

from .garment_extractor.commands import extract_garment_mask
from .human_segmentator import do_human_segmentation_inference
from .job_repository import JobRepository
from .job_scheduler import JobScheduler
from .jobs import PreprocessingJob
from .pose_extractor import extract_poses, run_densepose


@dataclass
class PreprocessingService:
    repository: JobRepository
    scheduler: JobScheduler
    BASE_FOLDER = os.getenv("BASE_FOLDER", "")
    IMAGE_SIZE = (768, 1024)

    def process(self, job_id: str):
        job = self.repository.find_by_id(UUID(job_id))
        if not job:
            raise ValueError(f"Job ID {job_id} not found")

        job.processing()
        self.repository.save(job)
        try:
            base_folder = os.path.join(self.BASE_FOLDER, job_id)
            # process garment
            garment_mask_output_file = os.path.join(base_folder, "garment_mask.jpg")
            garment_only_output_file = os.path.join(base_folder, "garment_only.jpg")
            extract_garment_mask(
                input_image=job.garment_image,
                output_mask_image=garment_mask_output_file,
                output_garment_only_image=garment_only_output_file,
            )

            # process ref image
            # poses
            pose_output_file = os.path.join(base_folder, "keypoints.json")
            extract_poses(input_file=job.ref_image, output_file=pose_output_file)

            # densepose
            densepose_output_file = os.path.join(base_folder, "densepose.jpg")
            run_densepose(
                input_file=job.ref_image,
                output_file=densepose_output_file,
            )

            # segmentation
            segmentation_output_file = os.path.join(base_folder, "segmented.jpg")
            do_human_segmentation_inference(
                img_path=job.ref_image,
                output_file=segmentation_output_file,
            )

            job.success_with(
                masked_garment_image=garment_only_output_file,
                densepose_image=densepose_output_file,
                segmented_image=segmentation_output_file,
                pose_keypoints=pose_output_file,
            )
            self.repository.save(job)
        except Exception as e:
            logging.error(f"Error while processing job ID {job_id}")
            logging.exception(e)
            job.failed()
            self.repository.save(job)

    def create_job(self, ref_image: Image.Image, garment_image: Image.Image):
        job_id = uuid4()
        base_folder = os.path.join(self.BASE_FOLDER, str(job_id))
        ref_image_file = os.path.join(base_folder, "ref.jpg")
        garment_image_file = os.path.join(base_folder, "garment.jpg")
        ref_image.resize(self.IMAGE_SIZE).save(ref_image_file)
        garment_image.resize(self.IMAGE_SIZE).save(garment_image_file)
        job = PreprocessingJob(
            ref_image=ref_image_file, garment_image=garment_image_file, id=job_id
        )
        self.scheduler.schedule(self.process, str(job_id))
        self.repository.save(job)
        return str(job.id)
    
    def get_job(self, job_id: str):
        return self.repository.find_by_id(UUID(job_id))

    def abort_job(self, job_id: str):
        job = self.repository.find_by_id(UUID(job_id))
        self.scheduler.abort(job_id)
        job.aborted()
        self.repository.save(job)
