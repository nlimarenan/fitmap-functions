package com.fitmap.function.gymcontext.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fitmap.function.common.exception.TerminalException;
import com.fitmap.function.common.service.CheckConstraintsRequestBodyService;
import com.fitmap.function.gymcontext.domain.Address;
import com.fitmap.function.gymcontext.domain.Contact;
import com.fitmap.function.gymcontext.domain.Event;
import com.fitmap.function.gymcontext.domain.Gym;
import com.fitmap.function.gymcontext.domain.SubscriptionPlan;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
@RequiredArgsConstructor
public class GymService {

    private static final String GYMS_COLLECTION = "gyms";
    private static final String CONTACTS_COLLECTION = "contacts";
    private static final String ADDRESS_COLLECTION = "addresses";
    private static final String EVENTS_COLLECTION = "events";
    private static final String SUBSCRIPTION_PLAN_COLLECTION = "subscription-plans";

    private final Firestore db;

    public Gym create(Gym gym) {

        var now = new Date();
        gym.setCreatedAt(now);
        gym.setUpdatedAt(now);

        var batch = db.batch();

        var gymDocRef = db.collection(GYMS_COLLECTION).document(gym.getId());

        var addressPerDocRef = gym.getAddresses().stream().map(address -> {
            var ref = gymDocRef.collection(ADDRESS_COLLECTION).document();
            address.setId(ref.getId());
            return Pair.of(address, ref);
        }).collect(Collectors.toSet());

        var contactsPerDocRef = gym.getContacts().stream().map(contact -> {
            var ref = gymDocRef.collection(CONTACTS_COLLECTION).document();
            contact.setId(ref.getId());
            return Pair.of(contact, ref);
        }).collect(Collectors.toSet());

        var eventsPerDocRef = gym.getEvents().stream().map(events -> {
            var ref = gymDocRef.collection(EVENTS_COLLECTION).document();
            events.setId(ref.getId());
            return Pair.of(events, ref);
        }).collect(Collectors.toSet());

        var subscriptionPlansPerDocRef = gym.getSubscriptionPlans().stream().map(subscriptionPlan -> {
            var ref = gymDocRef.collection(SUBSCRIPTION_PLAN_COLLECTION).document();
            subscriptionPlan.setId(ref.getId());
            return Pair.of(subscriptionPlan, ref);
        }).collect(Collectors.toSet());

        CheckConstraintsRequestBodyService.checkConstraints(gym);

        batch.create(gymDocRef, gym);
        addressPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        contactsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        eventsPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));
        subscriptionPlansPerDocRef.forEach(pair -> batch.create(pair.getRight(), pair.getLeft()));

        try {

            batch.commit().get();

            return gym;

        } catch (Exception e) {

            log.log(Level.SEVERE, e.getMessage(), e);

            throw new TerminalException(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    public List<Gym> find(List<String> gymIds) throws InterruptedException, ExecutionException {

        var gyms = new ArrayList<Gym>();

        db
            .collection(GYMS_COLLECTION)
            .whereIn(FieldPath.documentId(), gymIds)
            .get()
            .get()
            .forEach(queryDocSnapshot -> {

                try {

                    var docRef = queryDocSnapshot.getReference();
                    var contactsColl = docRef.collection(CONTACTS_COLLECTION).get();
                    var addressColl = docRef.collection(ADDRESS_COLLECTION).get();
                    var eventsColl = docRef.collection(EVENTS_COLLECTION).get();
                    var subscriptionPlansColl = docRef.collection(SUBSCRIPTION_PLAN_COLLECTION).get();

                    var gym = queryDocSnapshot.toObject(Gym.class);
                    var contacts = contactsColl.get().toObjects(Contact.class);
                    var addresses = addressColl.get().toObjects(Address.class);
                    var events = eventsColl.get().toObjects(Event.class);
                    var subscriptionPlans = subscriptionPlansColl.get().toObjects(SubscriptionPlan.class);

                    gym.addContacts(contacts);
                    gym.addAddresses(addresses);
                    gym.addEvents(events);
                    gym.addSubscriptionPlan(subscriptionPlans);
                    gyms.add(gym);

                } catch (Exception e) { }
            });

        return gyms;

    }

    public void updateProps(Gym gym) throws InterruptedException, ExecutionException {

        var docRef = db.collection(GYMS_COLLECTION).document(gym.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        if(StringUtils.isNotEmpty(gym.getBiography())) {
            propsToUpdate.put("biography", gym.getBiography());
        }


        if(gym.getGalleryPicturesUrls().size() > 0) {
        propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayUnion(gym.getGalleryPicturesUrls().toArray(new Object[gym.getGalleryPicturesUrls().size()])));
        }

        if(gym.getSports().size() > 0) {
        propsToUpdate.put("sports", FieldValue.arrayUnion(gym.getSports().toArray(new Object[gym.getSports().size()])));
        }

        docRef.update(propsToUpdate).get();
    }

    public void removeElementsFromArraysProps(Gym gym) throws InterruptedException, ExecutionException {

        var docRef = db.collection(GYMS_COLLECTION).document(gym.getId());

        var propsToUpdate = new HashMap<String, Object>();
        propsToUpdate.put("updatedAt", new Date());

        if(gym.getGalleryPicturesUrls().size() > 0) {
        propsToUpdate.put("galleryPicturesUrls", FieldValue.arrayRemove(gym.getGalleryPicturesUrls().toArray(new Object[gym.getGalleryPicturesUrls().size()])));
        }

        if(gym.getSports().size() > 0) {
        propsToUpdate.put("sports", FieldValue.arrayRemove(gym.getSports().toArray(new Object[gym.getSports().size()])));
        }

        docRef.update(propsToUpdate).get();
    }

}