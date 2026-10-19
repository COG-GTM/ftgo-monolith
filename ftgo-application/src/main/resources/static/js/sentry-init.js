/**
 * Sentry browser error reporting, session replay and user feedback for the
 * FTGO dashboard. Settings come from GET /config/sentry; Sentry stays
 * disabled when no DSN is configured.
 */
(function () {
  if (!window.Sentry) return;

  fetch('config/sentry', { headers: { Accept: 'application/json' } })
    .then(res => (res.ok ? res.json() : null))
    .then(config => {
      if (!config || !config.dsn) return;
      Sentry.init({
        dsn: config.dsn,
        environment: config.environment,
        sendDefaultPii: config.sendDefaultPii,
        integrations: [
          Sentry.browserTracingIntegration(),
          Sentry.replayIntegration({ maskAllText: true, maskAllInputs: true, blockAllMedia: true }),
          Sentry.feedbackIntegration({
            colorScheme: 'light',
            triggerLabel: 'Report a bug',
            formTitle: 'Report a bug',
            submitButtonLabel: 'Send bug report',
            enableScreenshot: false
          })
        ],
        tracesSampleRate: 1.0,
        replaysSessionSampleRate: 0,
        replaysOnErrorSampleRate: 1.0
      });
      Sentry.setTag('app', 'ftgo-dashboard');
    })
    .catch(err => console.warn('Sentry disabled:', err));
})();
