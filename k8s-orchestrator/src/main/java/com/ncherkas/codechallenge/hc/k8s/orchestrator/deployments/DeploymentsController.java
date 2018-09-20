package com.ncherkas.codechallenge.hc.k8s.orchestrator.deployments;

import com.google.common.collect.Streams;
import com.ncherkas.codechallenge.hc.k8s.orchestrator.PageResult;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * K8S Orchestrator REST API. Three methods are available: create, list-all and list-paginated.
 */
@RestController
@RequestMapping("/deployments")
public class DeploymentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeploymentsController.class);

    private static final String SORTING_PROPERTY = "createAt";

    private final DeploymentsRepository deploymentsRepository;

    @Autowired
    public DeploymentsController(DeploymentsRepository deploymentsRepository) {
        this.deploymentsRepository = checkNotNull(deploymentsRepository);
    }

    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    public void create(@RequestBody Deployment deployment) {
        LOGGER.debug("Creating new K8S deployment");
        deploymentsRepository.save(DeploymentEntity.of(deployment));
    }

    @RequestMapping(method = GET)
    public List<Deployment> listAll() {
        LOGGER.debug("Listing all K8S deployments");
        return toDeployments(deploymentsRepository.findAll(Sort.by(ASC, SORTING_PROPERTY)));
    }

    @RequestMapping(method = GET, params = {"page", "size"})
    public PageResult<Deployment> listPaginated(@RequestParam(value = "page") int page,
                                          @RequestParam(value = "size") int size) {

        LOGGER.debug("Listing K8S deployments: page {}, size {}", page, size);

        PageRequest pageRequest = PageRequest.of(page, size, ASC, SORTING_PROPERTY);

        Page<DeploymentEntity> results = deploymentsRepository.findAll(pageRequest);
        if (page >= results.getTotalPages()) {
            throw new ResourceNotFoundException(); // By default mapped to the 404
        }

        List<Deployment> deployments = toDeployments(results.getContent());

        return PageResult.of(results.getNumber(), results.getTotalPages(), results.getTotalElements(), deployments);
    }

    private static List<Deployment> toDeployments(Iterable<DeploymentEntity> entities) {
        return Streams.stream(entities)
                .map(DeploymentEntity::getDeployment)
                .collect(toList());
    }
}
