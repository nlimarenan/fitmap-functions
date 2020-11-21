package com.fitmap.function.gymcontext.v1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

import javax.validation.ConstraintViolationException;

import com.fitmap.function.common.config.FirestoreConfig;
import com.fitmap.function.common.config.SystemTimeZoneConfig;
import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.common.service.CheckRequestContentTypeService;
import com.fitmap.function.common.service.ReadRequestService;
import com.fitmap.function.common.service.ResponseService;
import com.fitmap.function.gymcontext.domain.Gym;
import com.fitmap.function.gymcontext.service.GymService;
import com.fitmap.function.gymcontext.v1.payload.request.CreateRequestDtos;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class CrudGymFunction implements HttpFunction {

    static {

        SystemTimeZoneConfig.setUtcDefaultTimeZone();
    }

    private final GymService gymService;

    public CrudGymFunction() {

        this.gymService = new GymService(FirestoreConfig.FIRESTORE);
    }

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {

        try {

            var requestMethod = HttpMethod.resolve(request.getMethod());

            switch (HttpMethod.resolve(request.getMethod())) {
                case GET:
                    doGet(request, response);
                    break;
                case POST:
                    doPost(request, response);
                    break;
                default:
                    throw new MethodNotAllowedException(requestMethod, Arrays.asList(HttpMethod.GET, HttpMethod.POST));
            }

        } catch (TerminalException e) {ResponseService.answerTerminalException(request, response, e);}
          catch (MethodNotAllowedException e) {ResponseService.answerMethodNotAllowed(request, response, e);}
          catch (UnsupportedMediaTypeStatusException e) {ResponseService.answerUnsupportedMediaType(request, response, e);}
          catch (HttpMessageNotReadableException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (ConstraintViolationException e) {ResponseService.answerBadRequest(request, response, e);}
          catch (Exception e) { log.log(Level.SEVERE, e.getMessage(), e); ResponseService.answerInternalServerError(request, response, e); }

    }

    private void doGet(HttpRequest request, HttpResponse response) throws Exception {

        var found = find(ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, found);
        ResponseService.fillResponseWithStatus(response, HttpStatus.OK);
    }

    private void doPost(HttpRequest request, HttpResponse response) {

        CheckRequestContentTypeService.checkApplicationJsonContentType(request);

        var dto = ReadRequestService.getBody(request, CreateRequestDtos.Gym.class);

        CheckConstraintsRequestBodyService.checkConstraints(dto);

        var created = create(dto, ReadRequestService.getUserId(request));

        ResponseService.writeResponse(response, created);
        ResponseService.fillResponseWithStatus(response, HttpStatus.CREATED);
    }

    private Gym create(final CreateRequestDtos.Gym dto, final String gymId) {

        var addresses = Objects.requireNonNullElse(dto.getAddresses(), new ArrayList<CreateRequestDtos.Address>());

        var contacts = Objects.requireNonNullElse(dto.getContacts(), new ArrayList<CreateRequestDtos.Contact>());

        var sports = Objects.requireNonNullElse(dto.getSports(), new ArrayList<String>());

        var galleryPicturesUrls = Objects.requireNonNullElse(dto.getGalleryPicturesUrls(), new ArrayList<String>());

        dto.setAddresses(addresses);
        dto.setContacts(contacts);
        dto.setSports(sports);
        dto.setGalleryPicturesUrls(galleryPicturesUrls);

        var gym = Gym.from(dto, gymId);

        return gymService.create(gym);
    }

    private Gym find(final String gymId) throws Exception {

        return gymService.find(gymId);
    }

}