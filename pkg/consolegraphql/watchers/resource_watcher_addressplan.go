/*
 * Copyright 2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by go generate; DO NOT EDIT.

package watchers

import (
	"fmt"
	tp "github.com/enmasseproject/enmasse/pkg/apis/admin/v1beta2"
	cp "github.com/enmasseproject/enmasse/pkg/client/clientset/versioned/typed/admin/v1beta2"
	"github.com/enmasseproject/enmasse/pkg/consolegraphql/cache"
	"k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/watch"
	"k8s.io/client-go/rest"
	"log"
	"reflect"
)

type AddressPlanWatcher struct {
	Namespace       string
	cache.Cache
	ClientInterface cp.AdminV1beta2Interface
	watching        chan struct{}
    watchingStarted bool
	stopchan        chan struct{}
	stoppedchan     chan struct{}
    create          func(*tp.AddressPlan) interface{}
    update          func(*tp.AddressPlan, interface{}) bool
}

func NewAddressPlanWatcher(c cache.Cache, namespace string, options... WatcherOption) (ResourceWatcher, error) {

    kw := &AddressPlanWatcher{
		Namespace:       namespace,
		Cache:           c,
		watching:        make(chan struct{}),
		stopchan:        make(chan struct{}),
		stoppedchan:     make(chan struct{}),
		create:          func(v *tp.AddressPlan) interface{} {
                             return v
                         },
	    update:          func(v *tp.AddressPlan, e interface{}) bool {
                             if !reflect.DeepEqual(v, e) {
                                 *e.(*tp.AddressPlan) = *v
                                 return true
                             } else {
                                 return false
                             }
                         },
    }

    for _, option := range options {
        option(kw)
	}

	if kw.ClientInterface == nil {
		return nil, fmt.Errorf("Client must be configured using the AddressPlanWatcherConfig or AddressPlanWatcherClient")
	}
	return kw, nil
}

func AddressPlanWatcherFactory(create func(*tp.AddressPlan) interface{}, update func(*tp.AddressPlan, interface{}) bool) WatcherOption {
	return func(watcher ResourceWatcher) error {
		w := watcher.(*AddressPlanWatcher)
		w.create = create
        w.update = update
        return nil
	}
}

func AddressPlanWatcherConfig(config *rest.Config) WatcherOption {
	return func(watcher ResourceWatcher) error {
		w := watcher.(*AddressPlanWatcher)

		var cl interface{}
		cl, _  = cp.NewForConfig(config)

		client, ok := cl.(cp.AdminV1beta2Interface)
		if !ok {
			return fmt.Errorf("unexpected type %T", cl)
		}

		w.ClientInterface = client
        return nil
	}
}

// Used to inject the fake client set for testing purposes
func AddressPlanWatcherClient(client cp.AdminV1beta2Interface) WatcherOption {
	return func(watcher ResourceWatcher) error {
		w := watcher.(*AddressPlanWatcher)
		w.ClientInterface = client
        return nil
	}
}

func (kw *AddressPlanWatcher) Watch() error {
	go func() {
		defer close(kw.stoppedchan)
		defer func() {
			if !kw.watchingStarted {
				close(kw.watching)
			}
		}()
		resource := kw.ClientInterface.AddressPlans(kw.Namespace)
		log.Printf("AddressPlan - Watching")
		running := true
		for running {
			err := kw.doWatch(resource)
			if err != nil {
				log.Printf("AddressPlan - Restarting watch")
			} else {
				running = false
			}
		}
		log.Printf("AddressPlan - Watching stopped")
	}()

	return nil
}

func (kw *AddressPlanWatcher) AwaitWatching() {
	<-kw.watching
}

func (kw *AddressPlanWatcher) Shutdown() {
	close(kw.stopchan)
	<-kw.stoppedchan
}

func (kw *AddressPlanWatcher) doWatch(resource cp.AddressPlanInterface) error {
	resourceList, err := resource.List(v1.ListOptions{})
	if err != nil {
		return err
	}

	curr, err := kw.Cache.GetMap("AddressPlan/", cache.UidKeyAccessor)

	var added = 0
	var updated = 0
	var unchanged = 0
	for _, res := range resourceList.Items {
		copy := res.DeepCopy()
		kw.updateKind(copy)

		if _, ok := curr[copy.UID]; ok {
			err = kw.Cache.Update(func (current interface{}) (interface{}, error) {
				if kw.update(copy, current) {
					updated++
					return copy, nil
				} else {
					unchanged++
					return nil, nil
				}
			}, copy)
			if err != nil {
				return err
			}
			delete(curr, copy.UID)
		} else {
			err = kw.Cache.Add(kw.create(copy))
			if err != nil {
				return err
			}
			added++
		}
	}

	// Now remove any stale
	for _, stale := range curr {
		err = kw.Cache.Delete(stale)
		if err != nil {
			return err
		}
	}
	var stale = len(curr)

	log.Printf("AddressPlan - Cache initialised population added %d, updated %d, unchanged %d, stale %d", added, updated, unchanged, stale)
	resourceWatch, err := resource.Watch(v1.ListOptions{
		ResourceVersion: resourceList.ResourceVersion,
	})

	if ! kw.watchingStarted {
		close(kw.watching)
		kw.watchingStarted = true
	}

	ch := resourceWatch.ResultChan()
	for {
		select {
		case event := <-ch:
			var err error
			if event.Type == watch.Error {
				err = fmt.Errorf("Watch ended in error")
			} else {
				res, ok := event.Object.(*tp.AddressPlan)
				log.Printf("AddressPlan - Received event type %s", event.Type)
				if !ok {
					err = fmt.Errorf("Watch error - object of unexpected type received")
				} else {
					copy := res.DeepCopy()
					kw.updateKind(copy)
					switch event.Type {
					case watch.Added:
						err = kw.Cache.Add(kw.create(copy))
					case watch.Modified:
						updatingKey := kw.create(copy)
						err = kw.Cache.Update(func (current interface{}) (interface{}, error) {
							if kw.update(copy, current) {
								return copy, nil
							} else {
								return nil, nil
							}
						}, updatingKey)
					case watch.Deleted:
						err = kw.Cache.Delete(kw.create(copy))
					}
				}
			}
			if err != nil {
				return err
			}
		case <-kw.stopchan:
			log.Printf("AddressPlan - Shutdown received")
			return nil
		}
	}
}

func (kw *AddressPlanWatcher) updateKind(o *tp.AddressPlan) {
	if o.TypeMeta.Kind == "" {
		o.TypeMeta.Kind = "AddressPlan"
	}
}
