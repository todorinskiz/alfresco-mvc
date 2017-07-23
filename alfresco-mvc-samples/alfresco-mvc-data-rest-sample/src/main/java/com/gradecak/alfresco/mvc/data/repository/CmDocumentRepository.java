package com.gradecak.alfresco.mvc.data.repository;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.gradecak.alfresco.mvc.data.support.AlfrescoNodeRepository;
import com.gradecak.alfresco.mvc.sample.domain.CmDocument;

@Repository
@Transactional
public interface CmDocumentRepository extends AlfrescoNodeRepository<CmDocument> {}
