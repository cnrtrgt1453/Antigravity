import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import { useAuthStore } from '../useAuthStore';
import { ApiAuthRepository } from '../../data/repositories/ApiAuthRepository';

// Mocking ApiAuthRepository
jest.mock('../../data/repositories/ApiAuthRepository');

describe('useAuthStore', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    useAuthStore.setState({
      user: null,
      isLoading: false,
      error: null,
    });
  });

  it('başlangıç değerleri doğru olmalıdır', () => {
    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.isLoading).toBe(false);
    expect(state.error).toBeNull();
  });

  it('login başarılı olduğunda kullanıcıyı set etmelidir', async () => {
    const mockUser = { id: '1', email: 'test@example.com', fullName: 'Test User' };
    
    // LoginUseCase içindeki execute metodunu tetikleyen repository metodunu mockla
    // useAuthStore içindeki loginUseCase repository'i kullanır.
    const mockLogin = jest.spyOn(ApiAuthRepository.prototype, 'login').mockResolvedValue(mockUser);

    await useAuthStore.getState().login('test@example.com', 'password123');

    const state = useAuthStore.getState();
    expect(state.user).toEqual(mockUser);
    expect(state.isLoading).toBe(false);
    expect(state.error).toBeNull();
  });

  it('login hatalı olduğunda error set etmelidir', async () => {
    jest.spyOn(ApiAuthRepository.prototype, 'login').mockRejectedValue(new Error('Giriş başarısız'));

    await useAuthStore.getState().login('wrong@email.com', 'wrongpass');

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(state.error).toBe('Giriş başarısız');
    expect(state.isLoading).toBe(false);
  });

  it('logout başarılı bir şekilde çıkış yapmalıdır', async () => {
    useAuthStore.setState({ user: { id: '1', email: 'a@b.com', fullName: 'A' } as any });
    const mockLogout = jest.spyOn(ApiAuthRepository.prototype, 'logout').mockResolvedValue();

    await useAuthStore.getState().logout();

    const state = useAuthStore.getState();
    expect(state.user).toBeNull();
    expect(mockLogout).toHaveBeenCalled();
  });
});
