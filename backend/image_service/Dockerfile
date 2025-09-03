FROM node:18-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN mkdir -p /app/uploads
EXPOSE 8080
CMD ["node", "server.js"]
