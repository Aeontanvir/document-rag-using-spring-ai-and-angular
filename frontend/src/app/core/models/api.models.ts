export interface AuthUser {
  id: string;
  name: string;
  email: string;
  createdAt: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}

export interface ProjectCreateRequest {
  name: string;
  description?: string;
}

export interface ProjectSummary {
  id: string;
  name: string;
  description: string | null;
  documentCount: number;
  conversationCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface ChatRequest {
  conversationId?: string;
  prompt: string;
  topK?: number;
}

export interface DocumentCitation {
  chunkId: string;
  projectId: string;
  documentId: string;
  sourceFileName: string;
  chunkIndex: number | null;
  excerpt: string;
}

export interface ChatResponse {
  conversationId: string;
  projectId: string;
  answer: string;
  citations: DocumentCitation[];
  respondedAt: string;
}

export interface ConversationMessage {
  id: number;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
}

export interface ConversationResponse {
  conversationId: string;
  projectId: string;
  projectName: string;
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ConversationMessage[];
}

export interface DocumentMetadata {
  id: string;
  projectId: string;
  projectName: string;
  originalFilename: string;
  mediaType: string;
  sizeBytes: number;
  checksum: string;
  chunkCount: number;
  status: 'INDEXING' | 'INDEXED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface UploadBatchResponse {
  uploadedCount: number;
  documents: DocumentMetadata[];
}

export interface ChatMessageViewModel {
  id: string;
  role: 'USER' | 'ASSISTANT';
  content: string;
  createdAt: string;
  citations: DocumentCitation[];
}
