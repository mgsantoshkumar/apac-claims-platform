FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
ARG CLAIMS_API_URL=http://localhost:18081
ARG NOTIFICATIONS_API_URL=http://localhost:18082
RUN printf "export const environment = {\n  production: true,\n  claimsApiUrl: '%s',\n  notificationsApiUrl: '%s'\n};\n" \
  "$CLAIMS_API_URL" "$NOTIFICATIONS_API_URL" > src/environments/environment.cloud.ts \
  && npm run build -- --configuration=production

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
