FROM node:18
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN mkdir -p /public
EXPOSE 8085
CMD ["node", "server.js"]