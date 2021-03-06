[//]: # (OHARRA: Meseez jarri esaldi bakoitza bere lerroan, Transifex-ek lerroak bere itzulpen eremuan jartzen ditu!)

## 3.4

  * Anonymous key download over Tor
  * Proxy support
  * Better YubiKey error handling

## 3.3

  * Azalpen ikusleiho berria
  * Agiri ugariren azalpena aldiberean
  * YubiKey akatsen kudeaketa hobea

## 3.2

  * Lehen bertsioa YubiKey sostengu osoarekin eskuragarri erabiltzaile interfazetik: Editatu giltzak, lotu YubiKey giltzekin,...
  Material diseinua
  * QR Kode eskaneatzea baterapena (Baimen berriak beharrezkoak)
  * Hobetuta giltza sortze laguntzailea
  * Zuzenduta harreman galera aldiberetu ondoren
  * Android 4 behar du
  * Giltza ikusleihoaren berdiseinua
  * Kripto hobespenen arruntzea, zifraketa seguruen hautaketa hobea
  * API: Sinadurak deserantsita, giltza sinatuaren hautaketa askea,...
  * Zuzenduta: Zenbait baliozko giltza ukatuta edo iraungituta erakusten dira
  * Ez da sinadurarik onartzen iraungitutako edo ukatutako azpigiltzetatik
  * Keybase.io sostengua ikuspegi aurreratuan
  * Giltza guztiak batera eguneratzeko metodoa


## 3.1.2

  * Zuzenduta esportatu giltzak agirietara (orain egitan)


## 3.1.1

  * Zuzenduta esportatu giltzak agirietara (partzialki idatzita)
  * Zuzenduta kraskatzea Android 2.3-an


## 3.1

  * Zuzenduta kraskatzea Android 5-ean
  * Egiaztagiri ikusleiho berria
  * Trukaketa Segurua zuzenean giltza zerrendatik (SafeSlinger liburutegia)
  * QR Kode programa berria
  * Dekriptaketa ikusleihoaren berdiseinua
  * Ikur berria eta margoak
  * Zuzenduta inportatu giltza sekretua Symantec Enkriptaketa Mahaigainetik
  * YubiKey sostengu esperimentala: Azpigiltza ID-ak orain zuzen egiaztatzen dira


## 3.0.1

  * Kudeaketa hobea giltza inportatze handietarako
  * Hobetuta azpigiltza hautapena


## 3.0

  * Eskaini bateragarritasun ezegonkorreko aplikazioak aplikazio zerrendan
  * Diseinu berria dekriptaketa ikusleihoentzat
  * Zuzenketa ugari giltza inportatzean, zuzenduta baita ere giltzen zuriketa
  * Ohoretu eta erakutsi giltza egiaztapen ikurrak
  * Erabiltzaile interfazea norbere giltzak sortzeko
  * Zuzenduta erabiltzaile id ukatze egiaztagiriak
  * Hodei bilaketa berria (ohiko giltza-zerbitzari eta keybase.io gain bilatzen da)
  * Sostengua giltza zuriketarako OpenKeychain barne
  * YubiKey sostengu esperimentala: Sostengua sinadura sortze eta dekriptaketarako


## 2.9.2

  * Zuzenduta 2.9.1-ko giltza haustea
  * YubiKey sostengu esperimentala. Dekriptaketak orain API bidez egiten du lan


## 2.9.1

  * Split encrypt screen into two
  * Fix key flags handling (now supporting Mailvelope 0.7 keys)
  * Improved passphrase handling
  * Key sharing via SafeSlinger
  * Experimental YubiKey support: Preference to allow other PINs, currently only signing via the OpenPGP API works, not inside of OpenKeychain
  * Fix usage of stripped keys
  * SHA256 as default for compatibility
  * Intent API has changed, see https://github.com/open-keychain/open-keychain/wiki/Intent-API
  * OpenPGP API now handles revoked/expired keys and returns all user ids


## 2.9

  * Fixing crashes introduced in v2.8
  * Experimental ECC support
  * Experimental YubiKey support: Only signing with imported keys


## 2.8

  * So many bugs have been fixed in this release that we focus on the main new features
  * Key edit: awesome new design, key revocation
  * Key import: awesome new design, secure keyserver connections via hkps, keyserver resolving via DNS SRV records
  * New first time screen
  * New key creation screen: autocompletion of name and email based on your personal Android accounts
  * File encryption: awesome new design, support for encrypting multiple files
  * New icons to show status of key (by Brennan Novak)
  * Important bug fix: Importing of large key collections from a file is now possible
  * Notification showing cached passphrases
  * Keys are connected to Android's contacts

This release wouldn't be possible without the work of Vincent Breitmoser (GSoC 2014), mar-v-in (GSoC 2014), Daniel Albert, Art O Cathain, Daniel Haß, Tim Bray, Thialfihar

## 2.7

  * Purple! (Dominik, Vincent)
  * New key view design (Dominik, Vincent)
  * New flat Android buttons (Dominik, Vincent)
  * API fixes (Dominik)
  * Keybase.io import (Tim Bray)


## 2.6.1

  * Some fixes for regression bugs


## 2.6

  * Key certifications (thanks to Vincent Breitmoser)
  * Support for GnuPG partial secret keys (thanks to Vincent Breitmoser)
  * New design for signature verification
  * Custom key length (thanks to Greg Witczak)
  * Fix share-functionality from other apps


## 2.5

  * Fix decryption of symmetric OpenPGP messages/files
  * Refactored key edit screen (thanks to Ash Hughes)
  * New modern design for encrypt/decrypt screens
  * OpenPGP API version 3 (multiple api accounts, internal fixes, key lookup)


## 2.4
Thanks to all applicants of Google Summer of Code 2014 who made this release feature rich and bug free!
Besides several small patches, a notable number of patches are made by the following people (in alphabetical order):
Daniel Hammann, Daniel Haß, Greg Witczak, Miroojin Bakshi, Nikhil Peter Raj, Paul Sarbinowski, Sreeram Boyapati, Vincent Breitmoser.

  * New unified key list
  * Colorized key fingerprint
  * Support for keyserver ports
  * Deactivate possibility to generate weak keys
  * Much more internal work on the API
  * Certify user ids
  * Keyserver query based on machine-readable output
  * Lock navigation drawer on tablets
  * Suggestions for emails on creation of keys
  * Search in public key lists
  * And much more improvements and fixes…


## 2.3.1

  * Hotfix for crash when upgrading from old versions


## 2.3

  * Remove unnecessary export of public keys when exporting secret key (thanks to Ash Hughes)
  * Fix setting expiry dates on keys (thanks to Ash Hughes)
  * More internal fixes when editing keys (thanks to Ash Hughes)
  * Querying keyservers directly from the import screen
  * Fix layout and dialog style on Android 2.2-3.0
  * Fix crash on keys with empty user ids
  * Fix crash and empty lists when coming back from signing screen
  * Bouncy Castle (cryptography library) updated from 1.47 to 1.50 and build from source
  * Fix upload of key from signing screen


## 2.2

  * New design with navigation drawer
  * New public key list design
  * New public key view
  * Bug fixes for importing of keys
  * Key cross-certification (thanks to Ash Hughes)
  * Handle UTF-8 passwords properly (thanks to Ash Hughes)
  * First version with new languages (thanks to the contributors on Transifex)
  * Sharing of keys via QR Codes fixed and improved
  * Package signature verification for API


## 2.1.1

  * API Updates, preparation for K-9 Mail integration


## 2.1

  * Lots of bug fixes
  * New API for developers
  * PRNG bug fix by Google


## 2.0

  * Complete redesign
  * Share public keys via QR codes, NFC beam
  * Sign keys
  * Upload keys to server
  * Fixes import issues
  * New AIDL API


## 1.0.8

  * Basic keyserver support
  * App2sd
  * More choices for passphrase cache: 1, 2, 4, 8, hours
  * Translations: Norwegian (thanks, Sander Danielsen), Chinese (thanks, Zhang Fredrick)
  * Bugfixes
  * Optimizations


## 1.0.7

  * Fixed problem with signature verification of texts with trailing newline
  * More options for passphrase cache time to live (20, 40, 60 mins)


## 1.0.6

  * Account adding crash on Froyo fixed
  * Secure file deletion
  * Option to delete key file after import
  * Stream encryption/decryption (gallery, etc.)
  * New options (language, force v3 signatures)
  * Interface changes
  * Bugfixes


## 1.0.5

  * German and Italian translation
  * Much smaller package, due to reduced BC sources
  * New preferences GUI
  * Layout adjustment for localization
  * Signature bugfix


## 1.0.4

  * Fixed another crash caused by some SDK bug with query builder


## 1.0.3

  * Fixed crashes during encryption/signing and possibly key export


## 1.0.2

  * Filterable key lists
  * Smarter pre-selection of encryption keys
  * New Intent handling for VIEW and SEND, allows files to be encrypted/decrypted out of file managers
  * Fixes and additional features (key preselection) for K-9 Mail, new beta build available


## 1.0.1

  * GMail account listing was broken in 1.0.0, fixed again


## 1.0.0

  * K-9 Mail integration, APG supporting beta build of K-9 Mail
  * Support of more file managers (including ASTRO)
  * Slovenian translation
  * New database, much faster, less memory usage
  * Defined Intents and content provider for other apps
  * Bugfixes