// Domain Entity — Firebase veya başka herhangi bir bağımlılıktan tamamen bağımsız saf model
export interface User {
  id: string;
  email: string;
  fullName: string;
  profilePictureUrl?: string;
}
