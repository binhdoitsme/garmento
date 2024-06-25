FROM node:22-alpine AS builder
COPY . /project
# RUN npm i -g yarn
WORKDIR /project
RUN yarn && yarn build

FROM builder
EXPOSE 3000
CMD [ "yarn", "start" ]
