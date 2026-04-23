import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectCreateRequest, ProjectSummary } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ProjectApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/projects';

  listProjects(): Observable<ProjectSummary[]> {
    return this.http.get<ProjectSummary[]>(this.baseUrl);
  }

  createProject(payload: ProjectCreateRequest): Observable<ProjectSummary> {
    return this.http.post<ProjectSummary>(this.baseUrl, payload);
  }

  deleteProject(projectId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${projectId}`);
  }
}
