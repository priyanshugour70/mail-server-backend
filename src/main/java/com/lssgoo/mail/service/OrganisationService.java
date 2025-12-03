package com.lssgoo.mail.service;

import com.lssgoo.mail.dtos.request.CreateOrganisationRequest;
import com.lssgoo.mail.dtos.request.UpdateOrganisationRequest;
import com.lssgoo.mail.dtos.response.OrganisationResponse;

import java.util.List;

public interface OrganisationService {

    OrganisationResponse createOrganisation(CreateOrganisationRequest request);

    OrganisationResponse getOrganisationById(Long id);

    OrganisationResponse getOrganisationByName(String name);

    OrganisationResponse getOrganisationByDomain(String domain);

    List<OrganisationResponse> getAllOrganisations();

    List<OrganisationResponse> getAllActiveOrganisations();

    OrganisationResponse updateOrganisation(Long id, UpdateOrganisationRequest request);

    void deleteOrganisation(Long id);

    void activateOrganisation(Long id);

    void deactivateOrganisation(Long id);
}

