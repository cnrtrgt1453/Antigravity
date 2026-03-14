// Domain Layer — Login Use Case (Single Responsibility: Sadece login iş mantığından sorumlu)
import { User } from '../entities/User';
import { AuthRepository } from '../repositories/AuthRepository';

export class LoginUseCase {
  constructor(private readonly authRepository: AuthRepository) {}

  async execute(email: string, password: string): Promise<User> {
    if (!email || !password) {
      throw new Error('E-posta ve şifre boş bırakılamaz.');
    }
    if (!email.includes('@')) {
      throw new Error('Geçerli bir e-posta adresi giriniz.');
    }
    return this.authRepository.login(email, password);
  }
}
