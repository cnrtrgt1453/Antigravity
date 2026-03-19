import { create } from 'zustand';
import { User } from '../../domain/entities/User';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';
import { RegisterUseCase } from '../../domain/usecases/RegisterUseCase';
import { ApiAuthRepository } from '../../data/repositories/ApiAuthRepository';

interface AuthState {
  user: User | null;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  loginWithGoogle: (idToken: string) => Promise<void>;
  register: (fullName: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

// Lazy-loaded dependencies to avoid top-level side effects in tests
let _authRepository: ApiAuthRepository | null = null;
let _loginUseCase: LoginUseCase | null = null;
let _registerUseCase: RegisterUseCase | null = null;

const getRepo = () => {
  if (!_authRepository) _authRepository = new ApiAuthRepository();
  return _authRepository;
};

const getLoginUseCase = () => {
  if (!_loginUseCase) _loginUseCase = new LoginUseCase(getRepo());
  return _loginUseCase;
};

const getRegisterUseCase = () => {
  if (!_registerUseCase) _registerUseCase = new RegisterUseCase(getRepo());
  return _registerUseCase;
};

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  error: null,
  isLoading: false,

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const user = await getLoginUseCase().execute(email, password);
      set({ user, isLoading: false });
    } catch (err: any) {
      set({ error: err.message || 'Giriş başarısız oldu.', isLoading: false });
      throw err;
    }
  },

  loginWithGoogle: async (idToken) => {
    set({ isLoading: true, error: null });
    try {
      const user = await getRepo().loginWithGoogle(idToken);
      set({ user, isLoading: false });
    } catch (err: any) {
      set({ error: err.message || 'Google girişi başarısız oldu.', isLoading: false });
      throw err;
    }
  },

  register: async (fullName, email, password) => {
    set({ isLoading: true, error: null });
    try {
      const user = await getRegisterUseCase().execute(fullName, email, password);
      set({ user, isLoading: false });
    } catch (err: any) {
      set({ error: err.message || 'Kayıt başarısız oldu.', isLoading: false });
      throw err;
    }
  },

  logout: async () => {
    try {
      await getRepo().logout();
    } catch (err) {
      console.error('Logout error:', err);
    } finally {
      set({ user: null });
    }
  },

  clearError: () => set({ error: null }),
}));
