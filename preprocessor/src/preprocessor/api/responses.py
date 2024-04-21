from pydantic import BaseModel


class JobResponse(BaseModel):
    id: str
    ref_image: str | None = None  # URL
    garment_image: str | None = None  # URL
    masked_garment_image: str | None = None  # cloth_mask
    densepose_image: str | None = None  # image-densepose
    segmented_image: str | None = None  # image-parse-v3
    pose_keypoints: str | None = None  # openpose_json
