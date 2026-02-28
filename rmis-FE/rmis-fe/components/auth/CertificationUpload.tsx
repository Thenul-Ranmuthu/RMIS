// components/auth/CertificationUpload.tsx
"use client";

import { useState, useRef } from "react";

interface Certification {
  certificationName: string;
  issuingAuthority: string;
  file: File | null;
}

interface CertificationUploadProps {
  certifications: Certification[];
  onChange: (certifications: Certification[]) => void;
  error?: string;
}

const UploadIcon = () => (
  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
  </svg>
);

const TrashIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
  </svg>
);

const FileIcon = () => (
  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
    <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
  </svg>
);

const ALLOWED_TYPES = ["application/pdf", "image/jpeg", "image/jpg", "image/png"];
const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

export default function CertificationUpload({ certifications, onChange, error }: CertificationUploadProps) {
  const [fileErrors, setFileErrors] = useState<{ [key: number]: string }>({});
  const fileInputRef = useRef<HTMLInputElement>(null);

  const validateFile = (file: File): string | null => {
    if (!ALLOWED_TYPES.includes(file.type)) {
      return "Invalid file format. Allowed: PDF, JPG, PNG";
    }
    if (file.size > MAX_FILE_SIZE) {
      return "File size exceeds 5MB limit";
    }
    return null;
  };

  const handleFileSelect = (index: number) => (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    const error = validateFile(file);
    if (error) {
      setFileErrors(prev => ({ ...prev, [index]: error }));
      return;
    }

    setFileErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors[index];
      return newErrors;
    });

    const updatedCertifications = [...certifications];
    updatedCertifications[index] = {
      ...updatedCertifications[index],
      file,
    };
    onChange(updatedCertifications);
  };

  const addCertification = () => {
    onChange([
      ...certifications,
      { certificationName: "", issuingAuthority: "", file: null },
    ]);
  };

  const removeCertification = (index: number) => {
    const updatedCertifications = certifications.filter((_, i) => i !== index);
    onChange(updatedCertifications);
    
    setFileErrors(prev => {
      const newErrors = { ...prev };
      delete newErrors[index];
      return newErrors;
    });
  };

  const updateCertField = (index: number, field: "certificationName" | "issuingAuthority", value: string) => {
    const updatedCertifications = [...certifications];
    updatedCertifications[index] = {
      ...updatedCertifications[index],
      [field]: value,
    };
    onChange(updatedCertifications);
  };

  const getFileName = (file: File | null) => {
    if (!file) return "No file chosen";
    if (file.name.length > 30) {
      return file.name.substring(0, 27) + "...";
    }
    return file.name;
  };

  return (
    <div className="certifications-container">
      {certifications.map((cert, index) => (
        <div key={index} className="certification-item">
          <div className="certification-header">
            <h4>Certification #{index + 1}</h4>
            {certifications.length > 1 && (
              <button
                type="button"
                className="remove-btn"
                onClick={() => removeCertification(index)}
              >
                <TrashIcon />
              </button>
            )}
          </div>

          <div className="row">
            <div className="field">
              <label htmlFor={`cert-name-${index}`}>Certification Name</label>
              <input
                id={`cert-name-${index}`}
                type="text"
                placeholder="e.g. HVAC Certification"
                value={cert.certificationName}
                onChange={(e) => updateCertField(index, "certificationName", e.target.value)}
              />
            </div>

            <div className="field">
              <label htmlFor={`issuing-auth-${index}`}>Issuing Authority</label>
              <input
                id={`issuing-auth-${index}`}
                type="text"
                placeholder="e.g. Ministry of Industry"
                value={cert.issuingAuthority}
                onChange={(e) => updateCertField(index, "issuingAuthority", e.target.value)}
              />
            </div>
          </div>

          <div className="file-upload-area">
            <input
              ref={fileInputRef}
              type="file"
              id={`file-${index}`}
              accept=".pdf,.jpg,.jpeg,.png"
              onChange={handleFileSelect(index)}
              style={{ display: "none" }}
            />
            
            <div className="file-input-wrapper">
              <button
                type="button"
                className="file-select-btn"
                onClick={() => {
                  const input = document.getElementById(`file-${index}`) as HTMLInputElement;
                  if (input) input.click();
                }}
              >
                <UploadIcon />
                Choose File
              </button>
              <span className={`file-name ${cert.file ? "has-file" : ""}`}>
                {getFileName(cert.file)}
              </span>
            </div>

            {fileErrors[index] && (
              <p className="file-error">{fileErrors[index]}</p>
            )}

            {cert.file && (
              <p className="file-success">
                <FileIcon />
                File ready for upload
              </p>
            )}
          </div>
        </div>
      ))}

      <button
        type="button"
        className="add-cert-btn"
        onClick={addCertification}
      >
        + Add Another Certification
      </button>

      {error && <p className="field-error">{error}</p>}

      <div className="upload-guidelines">
        <p><strong>Guidelines:</strong></p>
        <ul>
          <li>Accepted formats: PDF, JPG, PNG</li>
          <li>Maximum file size: 5MB per file</li>
          <li>Ensure documents are clear and legible</li>
          <li>Upload official certification documents only</li>
        </ul>
      </div>
    </div>
  );
}