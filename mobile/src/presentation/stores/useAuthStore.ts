// Presentation Layer — Auth Zustand Store (State Management)
// Sadece Domain UseCase'leri çağırır; Firebase veya veri kaynağı hakkında bilgisi yoktur.
import { create } from 'zustand';
import { User } from '../../domain/entities/User';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';
import { RegisterUseCase } from '../../domain/usecases/RegisterUseCase';
import { ApiAuthRepository } from '../../data/repositories/ApiAuthRepository';

// Dependency Injection: Repository burada bağlanıyor. Java Core API ile çalışacak.
const authRepository = new ApiAuthRepository();
const loginUseCase = new LoginUseCase(authRepository);
const registerUseCase = new RegisterUseCase(authRepository);

interface AuthState {
  user: User | null;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
  register: (fullName: string, email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isLoading: false,
  error: null,

  login: async (email: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const user = await loginUseCase.execute(email, password);
      // Başarılı giriş veritabanından döndüğü anda store'a 'user' olarak kazınır
      set({ user, isLoading: false });
    } catch (e: any) {
      set({ error: e.message, isLoading: false });
    }
  },

  register: async (fullName: string, email: string, password: string) => {
    set({ isLoading: true, error: null });
    try {
      const user = await registerUseCase.execute(fullName, email, password);
      // Başarılı kayıtta da kullanıcı doğrudan içeri alınır
      set({ user, isLoading: false });
    } catch (e: any) {
      set({ error: e.message, isLoading: false });
    }
  },

  logout: async () => {
    await authRepository.logout();
    set({ user: null, error: null });
  },

  clearError: () => set({ error: null }),
}));
