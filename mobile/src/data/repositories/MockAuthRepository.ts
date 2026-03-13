// Data Layer — Mock Auth Repository (Firebase entegrasyonundan ÖNCE test için)
// İleride bu sınıfın yerine FirebaseAuthRepository yazılacak; Domain ve UI katmanları değişmeyecek.
import { User } from '../../domain/entities/User';
import { AuthRepository } from '../../domain/repositories/AuthRepository';

const MOCK_USER: User = {
  uid: 'mock-uid-001',
  email: 'test@borsa.com',
  displayName: 'Test Kullanıcısı',
};

export class MockAuthRepository implements AuthRepository {
  private currentUser: User | null = null;

  async login(email: string, password: string): Promise<User> {
    // Gerçek Firebase entegrasyonu gelene kadar basit doğrulama
    if (email === 'test@borsa.com' && password === '123456') {
      this.currentUser = MOCK_USER;
      return MOCK_USER;
    }
    throw new Error('E-posta veya şifre hatalı.');
  }

  async logout(): Promise<void> {
    this.currentUser = null;
  }

  getCurrentUser(): User | null {
    return this.currentUser;
  }
}
