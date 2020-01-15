/*
 * Copyright 2018-2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by informer-gen. DO NOT EDIT.

package v1beta1

import (
	time "time"

	enmassev1beta1 "github.com/enmasseproject/enmasse/pkg/apis/enmasse/v1beta1"
	versioned "github.com/enmasseproject/enmasse/pkg/client/clientset/versioned"
	internalinterfaces "github.com/enmasseproject/enmasse/pkg/client/informers/externalversions/internalinterfaces"
	v1beta1 "github.com/enmasseproject/enmasse/pkg/client/listers/enmasse/v1beta1"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	runtime "k8s.io/apimachinery/pkg/runtime"
	watch "k8s.io/apimachinery/pkg/watch"
	cache "k8s.io/client-go/tools/cache"
)

// AddressSpaceSchemaInformer provides access to a shared informer and lister for
// AddressSpaceSchemas.
type AddressSpaceSchemaInformer interface {
	Informer() cache.SharedIndexInformer
	Lister() v1beta1.AddressSpaceSchemaLister
}

type addressSpaceSchemaInformer struct {
	factory          internalinterfaces.SharedInformerFactory
	tweakListOptions internalinterfaces.TweakListOptionsFunc
}

// NewAddressSpaceSchemaInformer constructs a new informer for AddressSpaceSchema type.
// Always prefer using an informer factory to get a shared informer instead of getting an independent
// one. This reduces memory footprint and number of connections to the server.
func NewAddressSpaceSchemaInformer(client versioned.Interface, resyncPeriod time.Duration, indexers cache.Indexers) cache.SharedIndexInformer {
	return NewFilteredAddressSpaceSchemaInformer(client, resyncPeriod, indexers, nil)
}

// NewFilteredAddressSpaceSchemaInformer constructs a new informer for AddressSpaceSchema type.
// Always prefer using an informer factory to get a shared informer instead of getting an independent
// one. This reduces memory footprint and number of connections to the server.
func NewFilteredAddressSpaceSchemaInformer(client versioned.Interface, resyncPeriod time.Duration, indexers cache.Indexers, tweakListOptions internalinterfaces.TweakListOptionsFunc) cache.SharedIndexInformer {
	return cache.NewSharedIndexInformer(
		&cache.ListWatch{
			ListFunc: func(options v1.ListOptions) (runtime.Object, error) {
				if tweakListOptions != nil {
					tweakListOptions(&options)
				}
				return client.EnmasseV1beta1().AddressSpaceSchemas().List(options)
			},
			WatchFunc: func(options v1.ListOptions) (watch.Interface, error) {
				if tweakListOptions != nil {
					tweakListOptions(&options)
				}
				return client.EnmasseV1beta1().AddressSpaceSchemas().Watch(options)
			},
		},
		&enmassev1beta1.AddressSpaceSchema{},
		resyncPeriod,
		indexers,
	)
}

func (f *addressSpaceSchemaInformer) defaultInformer(client versioned.Interface, resyncPeriod time.Duration) cache.SharedIndexInformer {
	return NewFilteredAddressSpaceSchemaInformer(client, resyncPeriod, cache.Indexers{cache.NamespaceIndex: cache.MetaNamespaceIndexFunc}, f.tweakListOptions)
}

func (f *addressSpaceSchemaInformer) Informer() cache.SharedIndexInformer {
	return f.factory.InformerFor(&enmassev1beta1.AddressSpaceSchema{}, f.defaultInformer)
}

func (f *addressSpaceSchemaInformer) Lister() v1beta1.AddressSpaceSchemaLister {
	return v1beta1.NewAddressSpaceSchemaLister(f.Informer().GetIndexer())
}
