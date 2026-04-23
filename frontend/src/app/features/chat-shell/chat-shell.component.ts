import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ChatMessageViewModel, DocumentMetadata, ProjectSummary } from '../../core/models/api.models';
import { ChatApiService } from '../../core/services/chat-api.service';
import { DocumentApiService } from '../../core/services/document-api.service';
import { ProjectApiService } from '../../core/services/project-api.service';
import { DocumentCardComponent } from '../../shared/components/document-card/document-card.component';
import { MessageBubbleComponent } from '../../shared/components/message-bubble/message-bubble.component';

@Component({
  selector: 'app-chat-shell',
  imports: [CommonModule, FormsModule, MessageBubbleComponent, DocumentCardComponent],
  templateUrl: './chat-shell.component.html',
  styleUrl: './chat-shell.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChatShellComponent {
  private readonly chatApi = inject(ChatApiService);
  private readonly documentApi = inject(DocumentApiService);
  private readonly projectApi = inject(ProjectApiService);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly projects = signal<ProjectSummary[]>([]);
  protected readonly selectedProjectId = signal<string | null>(null);
  protected readonly selectedProject = computed(
    () => this.projects().find((project) => project.id === this.selectedProjectId()) ?? null
  );
  protected readonly documents = signal<DocumentMetadata[]>([]);
  protected readonly messages = signal<ChatMessageViewModel[]>([]);
  protected readonly isUploading = signal(false);
  protected readonly isSending = signal(false);
  protected readonly isCreatingProject = signal(false);
  protected readonly isDeletingProject = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly uploadNotice = signal<string | null>(null);
  protected readonly conversationId = signal<string | null>(null);
  protected readonly documentCount = computed(() => this.documents().length);
  protected readonly projectCount = computed(() => this.projects().length);

  protected draft = '';
  protected projectNameDraft = '';
  protected projectDescriptionDraft = '';

  constructor() {
    this.loadProjects();
  }

  protected createProject(): void {
    const name = this.projectNameDraft.trim();
    if (!name) {
      this.errorMessage.set('Project name is required before you can create a workspace.');
      return;
    }

    this.isCreatingProject.set(true);
    this.errorMessage.set(null);

    this.projectApi
      .createProject({
        name,
        description: this.projectDescriptionDraft.trim() || undefined
      })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isCreatingProject.set(false))
      )
      .subscribe({
        next: (project) => {
          this.projectNameDraft = '';
          this.projectDescriptionDraft = '';
          this.uploadNotice.set(`Project "${project.name}" is ready for project-wise RAG.`);
          this.loadProjects(project.id);
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to create project.')
      });
  }

  protected selectProject(projectId: string): void {
    if (projectId === this.selectedProjectId()) {
      return;
    }

    this.selectedProjectId.set(projectId);
    this.resetConversation();
    this.errorMessage.set(null);
    this.uploadNotice.set(null);
    this.loadDocuments(projectId);
  }

  protected deleteProject(): void {
    const project = this.selectedProject();
    if (!project) {
      return;
    }

    this.isDeletingProject.set(true);
    this.errorMessage.set(null);

    this.projectApi
      .deleteProject(project.id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isDeletingProject.set(false))
      )
      .subscribe({
        next: () => {
          this.uploadNotice.set(`Project "${project.name}" was deleted.`);
          this.loadProjects();
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to delete project.')
      });
  }

  protected onFilesPicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];
    const projectId = this.selectedProjectId();

    if (!files.length) {
      return;
    }

    if (!projectId) {
      this.errorMessage.set('Create or select a project before uploading files.');
      input.value = '';
      return;
    }

    this.isUploading.set(true);
    this.uploadNotice.set(null);
    this.errorMessage.set(null);

    this.documentApi
      .uploadDocuments(projectId, files)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.isUploading.set(false);
          input.value = '';
        })
      )
      .subscribe({
        next: (response) => {
          this.uploadNotice.set(
            `${response.uploadedCount} file(s) indexed into ${this.selectedProject()?.name ?? 'the project'} knowledge base.`
          );
          this.loadProjects(projectId);
          this.loadDocuments(projectId);
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Document upload failed.')
      });
  }

  protected sendPrompt(): void {
    const prompt = this.draft.trim();
    const projectId = this.selectedProjectId();

    if (!projectId) {
      this.errorMessage.set('Select a project before starting a project-wise chat.');
      return;
    }

    if (!prompt || this.isSending()) {
      return;
    }

    this.errorMessage.set(null);
    this.isSending.set(true);
    this.messages.update((messages) => [...messages, this.toViewMessage('USER', prompt, new Date().toISOString())]);
    this.draft = '';

    this.chatApi
      .sendMessage(projectId, {
        conversationId: this.conversationId() ?? undefined,
        prompt,
        topK: 5
      })
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.isSending.set(false))
      )
      .subscribe({
        next: (response) => {
          this.conversationId.set(response.conversationId);
          this.messages.update((messages) => [
            ...messages,
            {
              id: crypto.randomUUID(),
              role: 'ASSISTANT',
              content: response.answer,
              createdAt: response.respondedAt,
              citations: response.citations
            }
          ]);
          this.loadProjects(projectId);
        },
        error: (error) => {
          this.messages.update((messages) => [
            ...messages,
            this.toViewMessage(
              'ASSISTANT',
              error.error?.message ?? 'The backend could not complete the project-scoped RAG request.',
              new Date().toISOString()
            )
          ]);
        }
      });
  }

  protected resetConversation(): void {
    this.messages.set([]);
    this.errorMessage.set(null);
    this.uploadNotice.set(null);
    this.draft = '';
    this.conversationId.set(null);
  }

  protected clearServerConversation(): void {
    const projectId = this.selectedProjectId();
    const currentConversationId = this.conversationId();

    if (!projectId || !currentConversationId) {
      this.resetConversation();
      return;
    }

    this.chatApi
      .deleteConversation(projectId, currentConversationId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => this.resetConversation(),
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to clear conversation.')
      });
  }

  protected removeDocument(documentId: string): void {
    const projectId = this.selectedProjectId();
    if (!projectId) {
      return;
    }

    this.documentApi
      .deleteDocument(projectId, documentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.loadProjects(projectId);
          this.loadDocuments(projectId);
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to delete document.')
      });
  }

  protected trackMessage(index: number, message: ChatMessageViewModel): string {
    return message.id;
  }

  protected trackDocument(index: number, document: DocumentMetadata): string {
    return document.id;
  }

  protected trackProject(index: number, project: ProjectSummary): string {
    return project.id;
  }

  private loadProjects(preferredProjectId?: string): void {
    this.projectApi
      .listProjects()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (projects) => {
          this.projects.set(projects);
          const nextProjectId = this.resolveSelectedProjectId(projects, preferredProjectId);
          const changedProject = nextProjectId !== this.selectedProjectId();

          this.selectedProjectId.set(nextProjectId);

          if (!nextProjectId) {
            this.documents.set([]);
            this.resetConversation();
            return;
          }

          if (changedProject) {
            this.resetConversation();
          }

          this.loadDocuments(nextProjectId);
        },
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to load projects.')
      });
  }

  private loadDocuments(projectId = this.selectedProjectId()): void {
    if (!projectId) {
      this.documents.set([]);
      return;
    }

    this.documentApi
      .listDocuments(projectId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (documents) => this.documents.set(documents),
        error: (error) => this.errorMessage.set(error.error?.message ?? 'Unable to load documents.')
      });
  }

  private resolveSelectedProjectId(projects: ProjectSummary[], preferredProjectId?: string): string | null {
    const currentProjectId = this.selectedProjectId();

    if (preferredProjectId && projects.some((project) => project.id === preferredProjectId)) {
      return preferredProjectId;
    }

    if (currentProjectId && projects.some((project) => project.id === currentProjectId)) {
      return currentProjectId;
    }

    return projects[0]?.id ?? null;
  }

  private toViewMessage(role: 'USER' | 'ASSISTANT', content: string, createdAt: string): ChatMessageViewModel {
    return {
      id: crypto.randomUUID(),
      role,
      content,
      createdAt,
      citations: []
    };
  }
}
