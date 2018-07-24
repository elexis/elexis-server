
# Using HTTPS for local development

Setting up HTTPS for local development is quite tricky, due to the certain security measures included (like HSTS).

The general approach for web-development is 

1.  Import the ElexisServerTestCA.pem file into your local certificate store and trust it
2.  Configure /etc/hosts to include an additional hostname for 127.0.0.1 (to avoid HSTS on original localhost)
3.  Include the ElexisServerTestCA certificate for whatever other tools you are using (e.g. node.js)


## Importing ElexisServerTestCA in OS X

To accept this certificate for development purposes, execute the following within the terminal where the file is located.

> sudo security import ElexisServerTestCA.pem

Then open the keychain manager and set the imported certificate "Test-Installation" to trust all.

## HSTS and localhost

Running ES on localhost, OpenID will (after first login) force HSTS for it - which is bad for development,
as we will not be able to access localhost using plain HTTP anymore. (You may see this when an ``ERR_SSL_PROTOCOL_ERRORERR_SSL_PROTOCOL_ERROR``
error message occurs)

See e.g. https://stackoverflow.com/questions/25277457/google-chrome-redirecting-localhost-to-https

Fix with <http://www.chasewoodford.com/blog/fixing-localhost-ssl-connection-error-in-google-chrome/>  - 
use <chrome://net-internals/#hsts> to manage Chrome HSTS settings.

### Solution

We have to host the client we are developing and the elexis-server on different _locahosts_. This can be realized by
modifying `/etc/hosts` like this

	127.0.0.1 localhost
	127.0.0.1 es.localhost

#### Web-App Development

npm config set strict-ssl false

npm config set strict-ssl false
npm set strict-ssl false
npm cache clean --force
NODE_EXTRA_CA_CERTS=ElexisServerTestCA.pem npm run dev

#### Links

https://ospi.fi/blog/trusting-your-own-certificate-authority.html
https://nodejs.org/api/cli.html


