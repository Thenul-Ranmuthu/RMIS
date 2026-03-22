"use client";

import { useState } from "react";
import { QuotaRequest } from "@/types/quota";
import { approveRequest, rejectRequest } from "@/services/quotaService";
import { getToken } from "@/services/authService";

interface ReviewModalProps {
  request: QuotaRequest;
  onClose: () => void;
  onSuccess: (message: string) => void;
}

export default function ReviewModal({
  request,
  onClose,
  onSuccess,
}: ReviewModalProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  //   const handle = async (action: "approve" | "reject") => {
  //     setError("");
  //     const token = getToken();
  //     if (!token) {
  //       setError("Session expired. Please log in again.");
  //       return;
  //     }
  //     setIsLoading(true);
  //     try {
  //       const message =
  //         action === "approve"
  //           ? await approveRequest(token, request.request_id)
  //           : await rejectRequest(token, request.request_id);
  //       onSuccess(message);
  //     } catch (err) {
  //       setError(
  //         err instanceof Error ? err.message : "Action failed. Please try again.",
  //       );
  //     } finally {
  //       setIsLoading(false);
  //     }
  //   };

  const handle = async (action: "approve" | "reject") => {
    console.log("handle called:", action, "request:", request); // ← add this
    console.log("request_id:", request.request_id); // ← add this
    setError("");
    const token = getToken();
    if (!token) {
      setError("Session expired. Please log in again.");
      return;
    }
    setIsLoading(true);
    try {
      const message =
        action === "approve"
          ? await approveRequest(token, request.request_id)
          : await rejectRequest(token, request.request_id);
      onSuccess(message); // ← this should close the modal and show toast
    } catch (err) {
      setError(err instanceof Error ? err.message : "Action failed.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{ backgroundColor: "rgba(0,0,0,0.5)" }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose();
      }}
    >
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8 relative">
        {/* Close */}
        <button
          onClick={onClose}
          disabled={isLoading}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600 transition disabled:opacity-40"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        {/* Header */}
        <div className="flex items-center gap-3 mb-6">
          <div className="bg-amber-100 rounded-xl p-3">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-6 w-6 text-amber-600"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
          </div>
          <div>
            <h2 className="text-xl font-black text-gray-900">
              Review Quota Request
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">
              Approve or reject this request
            </p>
          </div>
        </div>

        {/* Request summary */}
        <div className="bg-gray-50 rounded-xl p-4 mb-6 space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-500 font-medium">Request ID</span>
            <span className="font-bold text-gray-800">
              #{request.request_id}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 font-medium">Company</span>
            <span className="font-semibold text-gray-800">
              {request.company_name}
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 font-medium">Requested Quota</span>
            <span className="font-semibold text-gray-800">
              {request.requested_quota.toLocaleString()} tons
            </span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-500 font-medium">Submitted</span>
            <span className="font-semibold text-gray-800">
              {new Date(request.submission_date).toLocaleDateString("en-US", {
                month: "short",
                day: "numeric",
                year: "numeric",
              })}
            </span>
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="mb-4 flex items-start gap-2 p-3 bg-red-50 border border-red-200 rounded-xl">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-4 w-4 text-red-500 mt-0.5 flex-shrink-0"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
              />
            </svg>
            <p className="text-sm text-red-600">{error}</p>
          </div>
        )}

        {/* Actions */}
        <p className="text-sm text-gray-500 text-center mb-4">
          What would you like to do with this request?
        </p>
        <div className="flex gap-3">
          <button
            onClick={() => handle("reject")}
            disabled={isLoading}
            className="flex-1 flex items-center justify-center gap-2 py-3 rounded-xl border-2 border-red-200 text-red-600 font-bold text-sm hover:bg-red-50 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <svg
                className="animate-spin h-4 w-4"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                />
              </svg>
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            )}
            Reject
          </button>
          <button
            onClick={() => handle("approve")}
            disabled={isLoading}
            className="flex-1 flex items-center justify-center gap-2 py-3 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-sm shadow-lg shadow-emerald-200 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isLoading ? (
              <svg
                className="animate-spin h-4 w-4"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"
                />
              </svg>
            ) : (
              <svg
                xmlns="http://www.w3.org/2000/svg"
                className="h-4 w-4"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M5 13l4 4L19 7"
                />
              </svg>
            )}
            Approve
          </button>
        </div>

        <button
          onClick={onClose}
          disabled={isLoading}
          className="w-full mt-3 py-2.5 text-sm text-gray-500 hover:text-gray-700 transition disabled:opacity-40"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
