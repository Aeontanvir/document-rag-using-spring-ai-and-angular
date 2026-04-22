export interface ChatRequest {
  conversationId?: string;
  prompt: string;
  topK?: number;
}

export interface DocumentCitation {
  chunkId: string;
  documentId: string;
  sourceFileName: string;
  chunkIndex: number | null;
  excerpt: string;
}

export interface ChatResponse {
  conversationId: string;
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
  title: string;
  createdAt: string;
  updatedAt: string;
  messages: ConversationMessage[];
}

export interface DocumentMetadata {
  id: string;
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
