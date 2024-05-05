FROM node:alpine

WORKDIR /usr/src/app

ENV PATH /usr/src/app/node_modules/.bin:$PATH

RUN npm install -g @angular/cli

COPY package*.json ./

RUN npm install

COPY . .
CMD ["ng", "serve", "--host", "0.0.0.0"]
