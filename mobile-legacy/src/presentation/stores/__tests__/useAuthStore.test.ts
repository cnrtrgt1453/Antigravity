import { jest, describe, it, expect, beforeEach } from '@jest/globals';
import { useAuthStore } from '../useAuthStore';
import { ApiAuthRepository } from '../../../data/repositories/ApiAuthRepository';

jest.mock('../../../data/repositories/ApiAuthRepository');

describe('useAuthStore', () => {
  beforeEach(() => {
    // Store durumunu sıfırla
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
    
    // Global mock üzerinden metodun davranışını belirle
    // Not: ApiAuthRepository artık bir mock fonksiyonu döndüren mock'lanmış bir modüldür.
    // Sınıfın prototipini mocklayarak methodun dönüş değerini ayarla
    (ApiAuthRepository.prototype.login as jest.Mock).mockResolvedValue(mockUser);

    await useAuthStore.getState().login('test@example.com', 'password123');

    const state = useAuthStore.getState();
    expect(state.user).toEqual(mockUser);
    expect(state.isLoading).toBe(false);
    expect(state.error).toBeNull();
  });
});
