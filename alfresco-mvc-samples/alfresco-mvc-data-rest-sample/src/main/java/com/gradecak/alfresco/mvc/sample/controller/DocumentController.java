package com.gradecak.alfresco.mvc.sample.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.gradecak.alfresco.mvc.ControllerUtils;
import com.gradecak.alfresco.mvc.data.domain.CoreVersion;
import com.gradecak.alfresco.mvc.data.mapper.MapEntityMapper;
import com.gradecak.alfresco.mvc.data.rest.controller.HateaosUtils;
import com.pleosoft.alfresco.ibapp.domain.ContainerDocument;
import com.pleosoft.alfresco.ibapp.domain.CoreTask;
import com.pleosoft.alfresco.ibapp.domain.CoreWorkflow;
import com.pleosoft.alfresco.ibapp.domain.Note;
import com.pleosoft.alfresco.ibapp.mapper.ContainerDocumentPropertiesMapper;
import com.pleosoft.alfresco.ibapp.service.TenantDocumentService;

@Controller
@RequestMapping("/document")
public class DocumentController {
  private final TenantDocumentService documentService;
  private final ServiceRegistry serviceRegistry;
  private final ContainerDocumentPropertiesMapper mapper;

  @Autowired
  public DocumentController(TenantDocumentService documentService, ServiceRegistry serviceRegistry, ContainerDocumentPropertiesMapper mapper) {
    Assert.notNull(documentService);
    Assert.notNull(serviceRegistry);
    Assert.notNull(mapper);

    this.documentService = documentService;
    this.serviceRegistry = serviceRegistry;
    this.mapper = mapper;
  }

  @RequestMapping(value = "{documentId}", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> get(@PathVariable String documentId) {
    try {
      ContainerDocument document = documentService.get(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), mapper);
      return ControllerUtils.toResponseEntity(document);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "/history/{documentId}", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> version(@PathVariable String documentId) {
    try {
      ContainerDocument document = documentService.get(new NodeRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID, documentId), mapper);
      return ControllerUtils.toResponseEntity(document);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/versions", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> versions(@PathVariable String documentId) {
    try {
      List<CoreVersion> history = documentService.getVersions(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId));
      return ControllerUtils.toResponseEntity(history);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/{version}/v", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> history(@PathVariable String documentId, @PathVariable String version) {
    try {
      CoreVersion snapshot = documentService.getVersion(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), mapper, version);
      return ControllerUtils.toResponseEntity(snapshot);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}", method = { RequestMethod.DELETE })
  @ResponseBody
  public ResponseEntity<?> delete(@PathVariable String documentId) {
    try {
      documentService.delete(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId));
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}", method = { RequestMethod.PUT, RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> update(@PathVariable String documentId, @RequestParam("data") String data, @RequestParam(value = "filedata", required = false) final MultipartFile file) {
    try {
      MapEntityMapper mapper = new MapEntityMapper(serviceRegistry);
      Map<QName, Serializable> entityProperties = mapper.mapEntity(null, new ObjectMapper().readValue(data, new TypeReference<HashMap<String, Object>>() {}));
      documentService.update(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), entityProperties, file != null ? file.getInputStream() : null);
      return ControllerUtils.toEmptyResponseEntity();
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    } catch (Exception e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @RequestMapping(value = "{documentId}/notes", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getNotes(@PathVariable String documentId, Pageable pageable) {
    // TODO add pagination
    try {
      Page<Note> page = documentService.listNotes(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/notes", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createNote(@PathVariable String documentId, @RequestBody Note note) {
    try {
      Note n = documentService.createNote(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), note.getHtmlContent());
      return ControllerUtils.toResponseEntity(n);
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/workflows", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getWorkflows(@PathVariable String documentId, @RequestBody(required = false) HashMap<String, Object> properties, Pageable pageable) {
    // TODO add pagination
    try {
      Page<CoreWorkflow> page = documentService.listWorkflows(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), properties, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/workflows", method = { RequestMethod.POST })
  @ResponseBody
  public ResponseEntity<?> createWorkflow(@PathVariable String documentId, @RequestParam String processDef, @RequestBody(required = false) HashMap<String, Object> properties) {
    try {
      String createWorkflow = documentService.createWorkflow(processDef, new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), properties);
      return ControllerUtils.toResponseEntity(ImmutableMap.of("id", createWorkflow));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }

  @RequestMapping(value = "{documentId}/tasks", method = { RequestMethod.GET })
  @ResponseBody
  public ResponseEntity<?> getTasks(@PathVariable String documentId, @RequestBody(required = false) HashMap<String, Object> properties, Pageable pageable) {
    // TODO add pagination
    try {
      Page<CoreTask> page = documentService.listActiveTasks(new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, documentId), properties, pageable);
      return ControllerUtils.toResponseEntity(HateaosUtils.createPagedResource(page));
    } catch (AccessDeniedException e) {
      return ControllerUtils.toEmptyResponseEntity(HttpStatus.NOT_FOUND);
    }
  }
}
