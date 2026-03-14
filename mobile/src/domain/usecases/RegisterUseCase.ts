import { AuthRepository } from '../repositories/AuthRepository';
import { User } from '../entities/User';

export class RegisterUseCase {
  constructor(private readonly authRepository: AuthRepository) {}

  execute(fullName: string, email: string, password: string): Promise<User> {
    if (!fullName || !email || !password) {
      throw new Error('Ad soyad, e-posta ve şifre zorunludur.');
    }
    
    // E-posta formatı çok basit bir regex ile kontrol ediliyor.
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) {
      throw new Error('Geçerli bir e-posta adresi giriniz.');
    }

    if (password.length < 6) {
      throw new Error('Şifre en az 6 karakter olmalıdır.');
    }

    return this.authRepository.register(fullName, email, password);
  }
}
