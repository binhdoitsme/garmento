"""init tables

Revision ID: 4c420ab8eaeb
Revises: 
Create Date: 2024-04-22 00:41:11.845611

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = '4c420ab8eaeb'
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # ### commands auto generated by Alembic - please adjust! ###
    op.create_table('preprocessing_jobs',
    sa.Column('id', sa.String(length=64), nullable=False),
    sa.Column('status', sa.String(length=30), nullable=False),
    sa.Column('ref_image', sa.String(length=1024), nullable=False),
    sa.Column('garment_image', sa.String(length=1024), nullable=False),
    sa.Column('masked_garment_image', sa.String(length=1024), nullable=True),
    sa.Column('densepose_image', sa.String(length=1024), nullable=True),
    sa.Column('segmented_image', sa.String(length=1024), nullable=True),
    sa.Column('pose_keypoints', sa.String(length=1024), nullable=True),
    sa.PrimaryKeyConstraint('id')
    )
    # ### end Alembic commands ###


def downgrade() -> None:
    # ### commands auto generated by Alembic - please adjust! ###
    op.drop_table('preprocessing_jobs')
    # ### end Alembic commands ###