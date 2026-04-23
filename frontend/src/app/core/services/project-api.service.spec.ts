import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ProjectApiService } from './project-api.service';

describe('ProjectApiService', () => {
  let service: ProjectApiService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ProjectApiService, provideHttpClient(), provideHttpClientTesting()]
    });

    service = TestBed.inject(ProjectApiService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('lists projects from the projects endpoint', () => {
    service.listProjects().subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('creates a project with the supplied payload', () => {
    const payload = { name: 'Knowledge Hub', description: 'Shared docs' };

    service.createProject(payload).subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(payload);
    request.flush({ id: 'project-1' });
  });

  it('deletes a project by id', () => {
    service.deleteProject('project-1').subscribe();

    const request = httpTestingController.expectOne('/api/v1/projects/project-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
