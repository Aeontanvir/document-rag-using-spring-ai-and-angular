import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { DocumentApiService } from './document-api.service';

describe('DocumentApiService', () => {
  let service: DocumentApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DocumentApiService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(DocumentApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('lists documents for a project', () => {
    service.listDocuments('project-1').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/documents');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('uploads all selected files as multipart form data', () => {
    const firstFile = new File(['alpha'], 'alpha.txt', { type: 'text/plain' });
    const secondFile = new File(['beta'], 'beta.txt', { type: 'text/plain' });

    service.uploadDocuments('project-1', [firstFile, secondFile]).subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/documents/ingest');
    expect(request.request.method).toBe('POST');
    expect(request.request.body instanceof FormData).toBeTrue();
    const files = request.request.body.getAll('files') as File[];
    expect(files.map((file) => file.name)).toEqual(['alpha.txt', 'beta.txt']);
    request.flush({ uploadedCount: 2, documents: [] });
  });

  it('deletes a document by id', () => {
    service.deleteDocument('project-1', 'document-1').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1/documents/document-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
