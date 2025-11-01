const app = require('./app')
const config = require('./config/config')
const logger = require('./config/logger')

// SERVER CONFIGURATION
const host = config.app.host || '0.0.0.0'
const port = process.env.PORT || config.app.port || 3000
const prefix = config.app.prefix || ''

let server = null

// START SERVER
const startServer = () => {
  try {
    server = app.listen(port, host, () => {
      logger.info(`🚀 Server running at http://${host}:${port}${prefix}`)
      logger.info(`📦 Environment: ${config.env}`)
      logger.info(`⏰ Started at: ${new Date().toISOString()}`)
    })

    // Handle server errors
    server.on('error', (error) => {
      if (error.code === 'EADDRINUSE') {
        logger.error(`❌ Port ${port} is already in use. Please choose a different port.`)
      } else {
        logger.error('❌ Server error:', error)
      }
      process.exit(1)
    })

  } catch (error) {
    logger.error('❌ Failed to start server:', error)
    process.exit(1)
  }
}

// GRACEFUL SHUTDOWN
const gracefulShutdown = (signal) => {
  logger.info(`📴 Received signal ${signal}. Shutting down server...`)

  if (server) {
    server.close((error) => {
      if (error) {
        logger.error('❌ Error shutting down server:', error)
        process.exit(1)
      } else {
        logger.info('✅ Server shut down successfully')
        process.exit(0)
      }
    })

    // Force close after 10 seconds
    setTimeout(() => {
      logger.error('Timeout! Force closing server...')
      process.exit(1)
    }, 10000)
  } else {
    process.exit(0)
  }
}

// ERROR HANDLERS
const handleUnexpectedError = (error, source) => {
  logger.error(`❌ ${source || 'Unexpected error'}:`, {
    message: error.message,
    stack: error.stack,
    name: error.name,
    ...(error.details && { details: error.details })
  })

  // Đợi một chút để log được ghi xong trước khi shutdown
  setTimeout(() => {
    gracefulShutdown(source || 'UNCAUGHT_EXCEPTION')
  }, 2000)
}

// PROCESS EVENT LISTENERS
process.on('uncaughtException', (error) => handleUnexpectedError(error, 'UNCAUGHT_EXCEPTION'))
process.on('unhandledRejection', (error) => handleUnexpectedError(error, 'UNHANDLED_REJECTION'))
process.on('SIGTERM', () => gracefulShutdown('SIGTERM'))
process.on('SIGINT', () => gracefulShutdown('SIGINT'))

startServer()
