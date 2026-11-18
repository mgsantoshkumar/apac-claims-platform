export const environment = {
  production: true,
  claimsApiUrl: (window as unknown as { __CLAIMS_API_URL__?: string }).__CLAIMS_API_URL__ || 'http://localhost:18081',
  notificationsApiUrl: (window as unknown as { __NOTIFICATIONS_API_URL__?: string }).__NOTIFICATIONS_API_URL__ || 'http://localhost:18082'
};
