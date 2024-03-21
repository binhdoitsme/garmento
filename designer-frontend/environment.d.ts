declare global {
  namespace NodeJS {
    interface ProcessEnv {
      NODE_ENV: 'development' | 'production';
      REACT_APP_GOOGLE_CLIENT_ID: string;
      REACT_APP_GOOGLE_CLIENT_SECRET: string;
      PORT?: string;
    }
  }
}

// If this file has no import/export statements (i.e. is a script)
// convert it into a module by adding an empty export statement.
export {}