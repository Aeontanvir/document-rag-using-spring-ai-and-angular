import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentCardComponent } from './document-card.component';
import { DocumentMetadata } from '../../../core/models/api.models';

describe('DocumentCardComponent', () => {
  let fixture: ComponentFixture<DocumentCardComponent>;
  let component: DocumentCardComponent;

  const documentMetadata: DocumentMetadata = {
    id: 'document-1',
    projectId: 'project-1',
    projectName: 'Knowledge Hub',
    originalFilename: 'spring-ai-guide.pdf',
    mediaType: 'application/pdf',
    sizeBytes: 1536,
    checksum: 'abc123',
    chunkCount: 8,
    status: 'INDEXED',
    createdAt: '2026-04-23T10:00:00Z',
    updatedAt: '2026-04-23T10:00:00Z'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentCardComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(DocumentCardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('document', documentMetadata);
    fixture.detectChanges();
  });

  it('renders document details and formatted size', () => {
    const element = fixture.nativeElement as HTMLElement;

    expect(element.textContent).toContain('spring-ai-guide.pdf');
    expect(element.textContent).toContain('INDEXED');
    expect(element.textContent).toContain('8');
    expect(element.textContent).toContain('1.5 KB');
  });

  it('emits the document id when delete is clicked', () => {
    const emittedIds: string[] = [];
    component.remove.subscribe((documentId) => emittedIds.push(documentId));

    const button = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    button.click();

    expect(emittedIds).toEqual(['document-1']);
  });
});
