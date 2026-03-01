"use client";

import { useState, useEffect } from "react";
import Link from "next/link";

interface Certification {
  id: number;
  certificationName: string;
  issuingAuthority: string;
  fileType: string;
  fileUrl: string;
  originalFileName: string;
}

interface Technician {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  address: string;
  specialization: string;
  yearsOfExperience: number;
  status: string;
  registrationDate: string;
  approvalDate: string;
  certifications: Certification[];
}

// Sri Lankan districts array for reference (same as in signup)
const sriLankanDistricts = [
  "Ampara", "Anuradhapura", "Badulla", "Batticaloa", "Colombo",
  "Galle", "Gampaha", "Hambantota", "Jaffna", "Kalutara",
  "Kandy", "Kegalle", "Kilinochchi", "Kurunegala", "Mannar",
  "Matale", "Matara", "Monaragala", "Mullaitivu", "Nuwara Eliya",
  "Polonnaruwa", "Puttalam", "Ratnapura", "Trincomalee", "Vavuniya"
];

export default function PublicDirectory() {
  const [technicians, setTechnicians] = useState<Technician[]>([]);
  const [filteredTechnicians, setFilteredTechnicians] = useState<Technician[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  
  // Search and filter states
  const [searchTerm, setSearchTerm] = useState<string>("");
  const [selectedSpecialization, setSelectedSpecialization] = useState<string>("");
  const [selectedExperience, setSelectedExperience] = useState<string>("");
  const [selectedDistrict, setSelectedDistrict] = useState<string>("");
  
  // Pagination
  const [currentPage, setCurrentPage] = useState<number>(1);
  const [itemsPerPage] = useState<number>(6);
  
  // Helper function to extract district from address
  const extractDistrict = (address: string): string => {
    if (!address) return 'Unknown';
    
    // Split by comma and take the last part, then trim whitespace
    const parts = address.split(',').map(s => s.trim());
    const lastPart = parts[parts.length - 1];
    
    // Check if the last part matches any Sri Lankan district
    const matchedDistrict = sriLankanDistricts.find(district => 
      lastPart.toLowerCase().includes(district.toLowerCase())
    );
    
    return matchedDistrict || lastPart || 'Unknown';
  };
  
  // Get unique specializations for filter dropdown
  const specializations = Array.from(
    new Set(technicians.map(t => t.specialization).filter(Boolean))
  );
  
  // Get unique districts for filter dropdown
  const districts = Array.from(
    new Set(
      technicians
        .map(t => extractDistrict(t.address))
        .filter(d => d !== 'Unknown')
    )
  ).sort();

  useEffect(() => {
    fetchActiveTechnicians();
  }, []);

  useEffect(() => {
    filterTechnicians();
  }, [searchTerm, selectedSpecialization, selectedExperience, selectedDistrict, technicians]);

  const fetchActiveTechnicians = async () => {
    setIsLoading(true);
    setError("");
    
    try {
      // You'll need to create this endpoint on the backend
      const response = await fetch("http://localhost:8080/public/technicians/active");
      
      if (!response.ok) {
        throw new Error("Failed to fetch technicians");
      }
      
      const data = await response.json();
      setTechnicians(data);
      setFilteredTechnicians(data);
    } catch (error) {
      console.error("Error fetching technicians:", error);
      setError("Failed to load technicians. Please try again later.");
    } finally {
      setIsLoading(false);
    }
  };

  const filterTechnicians = () => {
    let filtered = [...technicians];
    
    // Search by name or specialization
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter(t => 
        t.firstName.toLowerCase().includes(term) ||
        t.lastName.toLowerCase().includes(term) ||
        (t.specialization && t.specialization.toLowerCase().includes(term))
      );
    }
    
    // Filter by specialization
    if (selectedSpecialization) {
      filtered = filtered.filter(t => t.specialization === selectedSpecialization);
    }
    
    // Filter by experience range
    if (selectedExperience) {
      const [min, max] = selectedExperience.split('-').map(Number);
      filtered = filtered.filter(t => {
        const exp = t.yearsOfExperience || 0;
        if (max) {
          return exp >= min && exp <= max;
        } else {
          return exp >= min; // For "10+ years"
        }
      });
    }
    
    // Filter by district
    if (selectedDistrict) {
      filtered = filtered.filter(t => {
        const district = extractDistrict(t.address);
        return district === selectedDistrict;
      });
    }
    
    setFilteredTechnicians(filtered);
    setCurrentPage(1);
  };

  const clearFilters = () => {
    setSearchTerm("");
    setSelectedSpecialization("");
    setSelectedExperience("");
    setSelectedDistrict("");
  };

  // Pagination
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredTechnicians.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredTechnicians.length / itemsPerPage);

  const handleViewProfile = (technicianId: number) => {
    // Navigate to technician public profile
    window.location.href = `/public/technician/${technicianId}`;
  };

  if (isLoading) {
    return (
      <div className="flex justify-center items-center min-h-[400px]">
        <div className="text-center">
          <svg className="animate-spin h-10 w-10 text-emerald-600 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <p className="text-gray-600">Loading technicians...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-xl p-8 text-center">
        <p className="text-red-600 mb-4">{error}</p>
        <button
          onClick={fetchActiveTechnicians}
          className="px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition"
        >
          Try Again
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
      {/* Header */}
      <div className="text-center mb-12">
        <h1 className="text-4xl font-black text-gray-900 mb-4">
          Find Certified Technicians
        </h1>
        <p className="text-xl text-gray-600 max-w-3xl mx-auto">
          Browse our directory of certified environmental compliance technicians across Sri Lanka
        </p>
      </div>

      {/* Search and Filters */}
      <div className="bg-white rounded-2xl shadow-lg p-6 mb-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
          {/* Search */}
          <div className="lg:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Search
            </label>
            <div className="relative">
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Search by name or specialization..."
                className="w-full rounded-lg border border-gray-300 pl-4 pr-10 py-2 focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
              />
              <svg
                className="absolute right-3 top-2.5 h-5 w-5 text-gray-400"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
          </div>

          {/* Specialization Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Specialization
            </label>
            <select
              value={selectedSpecialization}
              onChange={(e) => setSelectedSpecialization(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            >
              <option value="">All Specializations</option>
              {specializations.map(spec => (
                <option key={spec} value={spec}>{spec}</option>
              ))}
            </select>
          </div>

          {/* District Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              District
            </label>
            <select
              value={selectedDistrict}
              onChange={(e) => setSelectedDistrict(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            >
              <option value="">All Districts</option>
              {districts.map(district => (
                <option key={district} value={district}>{district}</option>
              ))}
            </select>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
          {/* Experience Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Experience
            </label>
            <select
              value={selectedExperience}
              onChange={(e) => setSelectedExperience(e.target.value)}
              className="w-full rounded-lg border border-gray-300 px-3 py-2 focus:ring-2 focus:ring-emerald-500 focus:border-transparent"
            >
              <option value="">Any Experience</option>
              <option value="0-2">0-2 years</option>
              <option value="3-5">3-5 years</option>
              <option value="6-10">6-10 years</option>
              <option value="10-100">10+ years</option>
            </select>
          </div>
          
          {/* Clear Filters Button */}
          <div className="flex items-end">
            <button
              onClick={clearFilters}
              className="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition"
            >
              Clear Filters
            </button>
          </div>
        </div>

        {/* Results Count */}
        <div className="mt-4 text-sm text-gray-600">
          Found {filteredTechnicians.length} technician{filteredTechnicians.length !== 1 ? 's' : ''}
        </div>
      </div>

      {/* Technician Grid */}
      {currentItems.length > 0 ? (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {currentItems.map((technician) => (
              <div
                key={technician.id}
                className="bg-white rounded-2xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow"
              >
                {/* Card Header - Avatar/Initials */}
                <div className="bg-gradient-to-r from-emerald-500 to-emerald-600 p-6 text-white">
                  <div className="flex items-center space-x-4">
                    <div className="h-16 w-16 bg-white rounded-full flex items-center justify-center">
                      <span className="text-2xl font-bold text-emerald-600">
                        {technician.firstName[0]}{technician.lastName[0]}
                      </span>
                    </div>
                    <div>
                      <h3 className="text-xl font-bold">
                        {technician.firstName} {technician.lastName}
                      </h3>
                      <p className="text-emerald-100">{technician.specialization || 'General Technician'}</p>
                    </div>
                  </div>
                </div>

                {/* Card Body */}
                <div className="p-6">
                  {/* Experience */}
                  <div className="flex items-center text-gray-600 mb-3">
                    <svg className="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>{technician.yearsOfExperience || 0} years experience</span>
                  </div>

                  {/* District */}
                  {technician.address && (
                    <div className="flex items-center text-gray-600 mb-3">
                      <svg className="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                      </svg>
                      <span className="truncate">District: {extractDistrict(technician.address)}</span>
                    </div>
                  )}

                  {/* Phone */}
                  <div className="flex items-center text-gray-600 mb-4">
                    <svg className="h-5 w-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                    <span>{technician.phoneNumber}</span>
                  </div>

                  {/* Certifications Preview */}
                  {technician.certifications && technician.certifications.length > 0 && (
                    <div className="mb-4">
                      <p className="text-sm font-medium text-gray-700 mb-2">Certifications:</p>
                      <div className="flex flex-wrap gap-2">
                        {technician.certifications.slice(0, 3).map((cert, idx) => (
                          <span
                            key={idx}
                            className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
                          >
                            {cert.certificationName}
                          </span>
                        ))}
                        {technician.certifications.length > 3 && (
                          <span className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full">
                            +{technician.certifications.length - 3} more
                          </span>
                        )}
                      </div>
                    </div>
                  )}

                  {/* View Profile Button */}
                  <button
                    onClick={() => handleViewProfile(technician.id)}
                    className="w-full mt-2 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition"
                  >
                    View Full Profile
                  </button>
                </div>
              </div>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex justify-center mt-8 space-x-2">
              <button
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Previous
              </button>
              
              {[...Array(totalPages)].map((_, i) => (
                <button
                  key={i + 1}
                  onClick={() => setCurrentPage(i + 1)}
                  className={`px-4 py-2 border rounded-lg ${
                    currentPage === i + 1
                      ? 'bg-emerald-600 text-white border-emerald-600'
                      : 'hover:bg-gray-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              
              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
                className="px-4 py-2 border rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                Next
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-12 bg-white rounded-2xl shadow">
          <svg
            className="mx-auto h-12 w-12 text-gray-400"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
            />
          </svg>
          <h3 className="mt-4 text-lg font-medium text-gray-900">No technicians found</h3>
          <p className="mt-2 text-gray-500">Try adjusting your search filters</p>
          <button
            onClick={clearFilters}
            className="mt-4 px-4 py-2 bg-emerald-600 text-white rounded-lg hover:bg-emerald-700 transition"
          >
            Clear Filters
          </button>
        </div>
      )}
    </div>
  );
}