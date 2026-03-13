// Domain Layer — Auth Repository Arayüzü
// Firebase veya başka herhangi bir Data Source'un bilgisi YOK. Sadece "ne yapılacağı" tanımlı.
import { User } from '../entities/User';

export interface AuthRepository {
  login(email: string, password: string): Promise<User>;
  logout(): Promise<void>;
  getCurrentUser(): User | null;
}
