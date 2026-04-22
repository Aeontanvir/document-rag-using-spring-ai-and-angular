import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DocumentMetadata, UploadBatchResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class DocumentApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/v1/documents';

  listDocuments(): Observable<DocumentMetadata[]> {
    return this.http.get<DocumentMetadata[]>(this.baseUrl);
  }

  uploadDocuments(files: File[]): Observable<UploadBatchResponse> {
    const formData = new FormData();
    for (const file of files) {
      formData.append('files', file, file.name);
    }
    return this.http.post<UploadBatchResponse>(`${this.baseUrl}/ingest`, formData);
  }

  deleteDocument(documentId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${documentId}`);
  }
}
