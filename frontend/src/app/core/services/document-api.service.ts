import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentMetadata, UploadBatchResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class DocumentApiService {
  private readonly http = inject(HttpClient);

  listDocuments(projectId: string): Observable<DocumentMetadata[]> {
    return this.http.get<DocumentMetadata[]>(this.baseUrl(projectId));
  }

  uploadDocuments(projectId: string, files: File[]): Observable<UploadBatchResponse> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file, file.name);
    }
    return this.http.post<UploadBatchResponse>(`${this.baseUrl(projectId)}/ingest`, formData);
  }

  deleteDocument(projectId: string, documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl(projectId)}/${documentId}`);
  }

  private baseUrl(projectId: string): string {
    return `/api/v1/projects/${projectId}/documents`;
  }
}
