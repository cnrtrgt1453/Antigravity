import { AuthRepository } from '../../domain/repositories/AuthRepository';
import { User } from '../../domain/entities/User';
import { Platform } from 'react-native';
import { Config } from '../../config';

const API_BASE_URL = `${Config.JAVA_API_URL}/api/v1/users`;

export class ApiAuthRepository implements AuthRepository {
  
  async login(email: string, password: string): Promise<User> {
    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ email, password }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || 'Login failed.');
      }

      const data = await response.json();
      
      // Java API, User objesini döndürüyor. Biz de kendi mobil User arayüzümüze mapliyoruz.
      return {
        id: data.id.toString(),
        email: data.email,
        fullName: data.fullName,
        profilePictureUrl: data.profilePictureUrl,
      };
    } catch (error) {
      console.error('Login Error:', error);
      throw error;
    }
  }

  async loginWithGoogle(idToken: string): Promise<User> {
    try {
      const response = await fetch(`${API_BASE_URL}/login/google`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ idToken }),
      });

      if (!response.ok) {
        throw new Error('Google giriş işlemi başarısız oldu.');
      }

      const data = await response.json();
      return {
        id: data.id.toString(),
        email: data.email,
        fullName: data.fullName,
        profilePictureUrl: data.profilePictureUrl,
      };
    } catch (error) {
      console.error('Google Login Error:', error);
      throw error;
    }
  }

  async register(fullName: string, email: string, password: string): Promise<User> {
    try {
      const response = await fetch(`${API_BASE_URL}/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
            fullName: fullName,
            email: email, 
            password: password 
        }),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);
        throw new Error(errorData?.message || 'Registration failed.');
      }

      const data = await response.json();
      
      return {
        id: data.id.toString(),
        email: data.email,
        fullName: data.fullName,
        profilePictureUrl: data.profilePictureUrl,
      };
    } catch (error) {
      console.error('Register Error:', error);
      throw error;
    }
  }

  async logout(): Promise<void> {
    // JWT/Token temizleme işlemi gerektiğinde buraya eklenebilir.
    return Promise.resolve();
  }

  getCurrentUser(): User | null {
    // Mobil tarafta state yönetimi Zustand üzerinden yapıldığı için, repository kendi State'ini tutmuyor.
    return null;
  }
}
