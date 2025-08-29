# Build stage
FROM node:18-alpine as build
WORKDIR /app

ARG REACT_APP_KEYCLOAK_URL
ARG REACT_APP_KEYCLOAK_REALM
ARG REACT_APP_KEYCLOAK_CLIENT
ENV REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL}
ENV REACT_APP_KEYCLOAK_REALM=${REACT_APP_KEYCLOAK_REALM}
ENV REACT_APP_KEYCLOAK_CLIENT=${REACT_APP_KEYCLOAK_CLIENT}

COPY package*.json ./
RUN npm ci
COPY . .

# Log environment variables for debugging
RUN echo "Building with: REACT_APP_KEYCLOAK_URL=${REACT_APP_KEYCLOAK_URL}"
RUN npm run build

# Production stage
FROM nginx:alpine
ENV TYPE "index"
COPY --from=build /app/build /usr/share/nginx/html
RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/templates/default.conf.template
CMD ["nginx", "-g", "daemon off;"]
