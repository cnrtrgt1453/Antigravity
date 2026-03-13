// Domain Entity — Firebase veya başka herhangi bir bağımlılıktan tamamen bağımsız saf model
export interface User {
  uid: string;
  email: string;
  displayName?: string;
}
