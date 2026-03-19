module.exports = {
  GoogleSignin: {
    configure: jest.fn(),
    hasPlayServices: jest.fn(() => Promise.resolve(true)),
    signIn: jest.fn(() => Promise.resolve({
      data: {
        user: {
          id: 'test-id',
          name: 'Test User',
          email: 'test@example.com',
          photo: null,
          familyName: 'User',
          givenName: 'Test',
        },
        idToken: 'test-token',
      }
    })),
    signOut: jest.fn(() => Promise.resolve()),
    isSignedIn: jest.fn(() => Promise.resolve(true)),
    getTokens: jest.fn(() => Promise.resolve({ idToken: 'test-token', accessToken: 'test-access-token' })),
  },
  statusCodes: {
    SIGN_IN_CANCELLED: 'SIGN_IN_CANCELLED',
    IN_PROGRESS: 'IN_PROGRESS',
    PLAY_SERVICES_NOT_AVAILABLE: 'PLAY_SERVICES_NOT_AVAILABLE',
    SIGN_IN_REQUIRED: 'SIGN_IN_REQUIRED',
  }
};
