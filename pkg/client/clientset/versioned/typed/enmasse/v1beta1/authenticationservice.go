/*
 * Copyright 2018-2019, EnMasse authors.
 * License: Apache License 2.0 (see the file LICENSE or http://apache.org/licenses/LICENSE-2.0.html).
 */

// Code generated by client-gen. DO NOT EDIT.

package v1beta1

import (
	"time"

	v1beta1 "github.com/enmasseproject/enmasse/pkg/apis/enmasse/v1beta1"
	scheme "github.com/enmasseproject/enmasse/pkg/client/clientset/versioned/scheme"
	v1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	types "k8s.io/apimachinery/pkg/types"
	watch "k8s.io/apimachinery/pkg/watch"
	rest "k8s.io/client-go/rest"
)

// AuthenticationServicesGetter has a method to return a AuthenticationServiceInterface.
// A group's client should implement this interface.
type AuthenticationServicesGetter interface {
	AuthenticationServices(namespace string) AuthenticationServiceInterface
}

// AuthenticationServiceInterface has methods to work with AuthenticationService resources.
type AuthenticationServiceInterface interface {
	Create(*v1beta1.AuthenticationService) (*v1beta1.AuthenticationService, error)
	Update(*v1beta1.AuthenticationService) (*v1beta1.AuthenticationService, error)
	Delete(name string, options *v1.DeleteOptions) error
	DeleteCollection(options *v1.DeleteOptions, listOptions v1.ListOptions) error
	Get(name string, options v1.GetOptions) (*v1beta1.AuthenticationService, error)
	List(opts v1.ListOptions) (*v1beta1.AuthenticationServiceList, error)
	Watch(opts v1.ListOptions) (watch.Interface, error)
	Patch(name string, pt types.PatchType, data []byte, subresources ...string) (result *v1beta1.AuthenticationService, err error)
	AuthenticationServiceExpansion
}

// authenticationServices implements AuthenticationServiceInterface
type authenticationServices struct {
	client rest.Interface
	ns     string
}

// newAuthenticationServices returns a AuthenticationServices
func newAuthenticationServices(c *EnmasseV1beta1Client, namespace string) *authenticationServices {
	return &authenticationServices{
		client: c.RESTClient(),
		ns:     namespace,
	}
}

// Get takes name of the authenticationService, and returns the corresponding authenticationService object, and an error if there is any.
func (c *authenticationServices) Get(name string, options v1.GetOptions) (result *v1beta1.AuthenticationService, err error) {
	result = &v1beta1.AuthenticationService{}
	err = c.client.Get().
		Namespace(c.ns).
		Resource("authenticationservices").
		Name(name).
		VersionedParams(&options, scheme.ParameterCodec).
		Do().
		Into(result)
	return
}

// List takes label and field selectors, and returns the list of AuthenticationServices that match those selectors.
func (c *authenticationServices) List(opts v1.ListOptions) (result *v1beta1.AuthenticationServiceList, err error) {
	var timeout time.Duration
	if opts.TimeoutSeconds != nil {
		timeout = time.Duration(*opts.TimeoutSeconds) * time.Second
	}
	result = &v1beta1.AuthenticationServiceList{}
	err = c.client.Get().
		Namespace(c.ns).
		Resource("authenticationservices").
		VersionedParams(&opts, scheme.ParameterCodec).
		Timeout(timeout).
		Do().
		Into(result)
	return
}

// Watch returns a watch.Interface that watches the requested authenticationServices.
func (c *authenticationServices) Watch(opts v1.ListOptions) (watch.Interface, error) {
	var timeout time.Duration
	if opts.TimeoutSeconds != nil {
		timeout = time.Duration(*opts.TimeoutSeconds) * time.Second
	}
	opts.Watch = true
	return c.client.Get().
		Namespace(c.ns).
		Resource("authenticationservices").
		VersionedParams(&opts, scheme.ParameterCodec).
		Timeout(timeout).
		Watch()
}

// Create takes the representation of a authenticationService and creates it.  Returns the server's representation of the authenticationService, and an error, if there is any.
func (c *authenticationServices) Create(authenticationService *v1beta1.AuthenticationService) (result *v1beta1.AuthenticationService, err error) {
	result = &v1beta1.AuthenticationService{}
	err = c.client.Post().
		Namespace(c.ns).
		Resource("authenticationservices").
		Body(authenticationService).
		Do().
		Into(result)
	return
}

// Update takes the representation of a authenticationService and updates it. Returns the server's representation of the authenticationService, and an error, if there is any.
func (c *authenticationServices) Update(authenticationService *v1beta1.AuthenticationService) (result *v1beta1.AuthenticationService, err error) {
	result = &v1beta1.AuthenticationService{}
	err = c.client.Put().
		Namespace(c.ns).
		Resource("authenticationservices").
		Name(authenticationService.Name).
		Body(authenticationService).
		Do().
		Into(result)
	return
}

// Delete takes name of the authenticationService and deletes it. Returns an error if one occurs.
func (c *authenticationServices) Delete(name string, options *v1.DeleteOptions) error {
	return c.client.Delete().
		Namespace(c.ns).
		Resource("authenticationservices").
		Name(name).
		Body(options).
		Do().
		Error()
}

// DeleteCollection deletes a collection of objects.
func (c *authenticationServices) DeleteCollection(options *v1.DeleteOptions, listOptions v1.ListOptions) error {
	var timeout time.Duration
	if listOptions.TimeoutSeconds != nil {
		timeout = time.Duration(*listOptions.TimeoutSeconds) * time.Second
	}
	return c.client.Delete().
		Namespace(c.ns).
		Resource("authenticationservices").
		VersionedParams(&listOptions, scheme.ParameterCodec).
		Timeout(timeout).
		Body(options).
		Do().
		Error()
}

// Patch applies the patch and returns the patched authenticationService.
func (c *authenticationServices) Patch(name string, pt types.PatchType, data []byte, subresources ...string) (result *v1beta1.AuthenticationService, err error) {
	result = &v1beta1.AuthenticationService{}
	err = c.client.Patch(pt).
		Namespace(c.ns).
		Resource("authenticationservices").
		SubResource(subresources...).
		Name(name).
		Body(data).
		Do().
		Into(result)
	return
}
