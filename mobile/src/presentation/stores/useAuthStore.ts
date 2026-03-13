// Presentation Layer — Auth Zustand Store (State Management)
// Sadece Domain UseCase'leri çağırır; Firebase veya veri kaynağı hakkında bilgisi yoktur.
import { create } from 'zustand';
import { User } from '../../domain/entities/User';
import { LoginUseCase } from '../../domain/usecases/LoginUseCase';
import { MockAuthRepository } from '../../data/repositories/MockAuthRepository';

// Dependency Injection: Repository burada bağlanıyor. Firebase hazır olunca sadece burası değişecek.
const authRepository = new MockAuthRepository();
const loginUseCase = new LoginUseCase(authRepository);

interface AuthState {
  user: User | null;
  isLoading: boolean;
  error: string | null;
  login: (email: string, password: string) => Promise<void>;
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
