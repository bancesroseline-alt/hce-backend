import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AlertaPrediccion {
  paciente?: {
    nombres?: string;
    apellidos?: string;
  };
  probabilidadInasistencia: number;
  nivelRiesgo: string;
  recomendacion: string;
}

@Injectable({
  providedIn: 'root'
})
export class PrediccionService {

  private readonly api = 'https://hce-backend.onrender.com/api/predicciones';

  constructor(private http: HttpClient) {}

  obtenerAlertas(): Observable<AlertaPrediccion[]> {
    return this.http.get<AlertaPrediccion[]>(`${this.api}/alertas`);
  }

  obtenerPorPaciente(id: number): Observable<AlertaPrediccion[]> {
    return this.http.get<AlertaPrediccion[]>(`${this.api}/paciente/${id}`);
  }
}
