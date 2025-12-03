package com.lssgoo.mail.service.impl;

import com.lssgoo.mail.dtos.request.CreateOrganisationRequest;
import com.lssgoo.mail.dtos.request.UpdateOrganisationRequest;
import com.lssgoo.mail.dtos.response.OrganisationResponse;
import com.lssgoo.mail.entity.AuditLog;
import com.lssgoo.mail.entity.Organisation;
import com.lssgoo.mail.repository.AuditLogRepository;
import com.lssgoo.mail.repository.OrganisationRepository;
import com.lssgoo.mail.service.OrganisationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrganisationServiceImpl implements OrganisationService {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public OrganisationResponse createOrganisation(CreateOrganisationRequest request) {
        // Check if organisation with same name exists
        if (organisationRepository.existsByName(request.getName())) {
            throw new RuntimeException("Organisation with name '" + request.getName() + "' already exists");
        }

        // Check if domain is provided and if it already exists
        if (request.getDomain() != null && !request.getDomain().isEmpty()) {
            if (organisationRepository.existsByDomain(request.getDomain())) {
                throw new RuntimeException("Organisation with domain '" + request.getDomain() + "' already exists");
            }
        }

        // Create new organisation
        Organisation organisation = new Organisation();
        organisation.setName(request.getName());
        organisation.setDomain(request.getDomain());
        organisation.setDescription(request.getDescription());
        organisation.setIsActive(true);

        organisation = organisationRepository.save(organisation);

        // Create audit log
        createAuditLog(null, null, "ORGANISATION_CREATED", "Organisation", organisation.getId(),
                "Organisation created: " + organisation.getName(), null);

        return buildOrganisationResponse(organisation);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationById(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with id: " + id));
        return buildOrganisationResponse(organisation);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationByName(String name) {
        Organisation organisation = organisationRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Organisation not found with name: " + name));
        return buildOrganisationResponse(organisation);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationByDomain(String domain) {
        Organisation organisation = organisationRepository.findByDomain(domain)
                .orElseThrow(() -> new RuntimeException("Organisation not found with domain: " + domain));
        return buildOrganisationResponse(organisation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisationResponse> getAllOrganisations() {
        return organisationRepository.findAll().stream()
                .map(this::buildOrganisationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrganisationResponse> getAllActiveOrganisations() {
        return organisationRepository.findAll().stream()
                .filter(org -> Boolean.TRUE.equals(org.getIsActive()))
                .map(this::buildOrganisationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrganisationResponse updateOrganisation(Long id, UpdateOrganisationRequest request) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (request.getName() != null && !request.getName().equals(organisation.getName())) {
            if (organisationRepository.existsByName(request.getName())) {
                throw new RuntimeException("Organisation with name '" + request.getName() + "' already exists");
            }
            organisation.setName(request.getName());
        }

        // Check if domain is being changed and if new domain already exists
        if (request.getDomain() != null && !request.getDomain().equals(organisation.getDomain())) {
            if (organisationRepository.existsByDomain(request.getDomain())) {
                throw new RuntimeException("Organisation with domain '" + request.getDomain() + "' already exists");
            }
            organisation.setDomain(request.getDomain());
        }

        if (request.getDescription() != null) {
            organisation.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            organisation.setIsActive(request.getIsActive());
        }

        organisation = organisationRepository.save(organisation);

        // Create audit log
        createAuditLog(null, null, "ORGANISATION_UPDATED", "Organisation", organisation.getId(),
                "Organisation updated: " + organisation.getName(), null);

        return buildOrganisationResponse(organisation);
    }

    @Override
    @Transactional
    public void deleteOrganisation(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with id: " + id));

        // Check if organisation has users
        if (organisation.getUsers() != null && !organisation.getUsers().isEmpty()) {
            throw new RuntimeException("Cannot delete organisation with existing users. Please remove users first or deactivate the organisation.");
        }

        organisationRepository.delete(organisation);

        // Create audit log
        createAuditLog(null, null, "ORGANISATION_DELETED", "Organisation", id,
                "Organisation deleted: " + organisation.getName(), null);
    }

    @Override
    @Transactional
    public void activateOrganisation(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with id: " + id));

        organisation.setIsActive(true);
        organisationRepository.save(organisation);

        // Create audit log
        createAuditLog(null, null, "ORGANISATION_ACTIVATED", "Organisation", organisation.getId(),
                "Organisation activated: " + organisation.getName(), null);
    }

    @Override
    @Transactional
    public void deactivateOrganisation(Long id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation not found with id: " + id));

        organisation.setIsActive(false);
        organisationRepository.save(organisation);

        // Create audit log
        createAuditLog(null, null, "ORGANISATION_DEACTIVATED", "Organisation", organisation.getId(),
                "Organisation deactivated: " + organisation.getName(), null);
    }

    private OrganisationResponse buildOrganisationResponse(Organisation organisation) {
        return OrganisationResponse.builder()
                .id(organisation.getId())
                .name(organisation.getName())
                .domain(organisation.getDomain())
                .description(organisation.getDescription())
                .isActive(organisation.getIsActive())
                .createdAt(organisation.getCreatedAt())
                .updatedAt(organisation.getUpdatedAt())
                .build();
    }

    private void createAuditLog(com.lssgoo.mail.entity.User user, com.lssgoo.mail.entity.Session session, String action, String entityType,
                               Long entityId, String description, HttpServletRequest httpRequest) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setSession(session);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        if (httpRequest != null) {
            auditLog.setIpAddress(getClientIpAddress(httpRequest));
            auditLog.setUserAgent(httpRequest.getHeader("User-Agent"));
            auditLog.setRequestMethod(httpRequest.getMethod());
            auditLog.setRequestUrl(httpRequest.getRequestURI());
        }
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}

