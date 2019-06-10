//#define MAISON

#ifdef MAISON

  #define SECRET_SSID "BOX_DE_LA_MAISON"
  #define SECRET_PASS "MOT_DE_PASSE_DE_LA_MAISON"
  #define WEP
  #define KEY_INDEX 1

#else

  #define SECRET_SSID "NOM_DU_PARTAGE_DE_CONNEXION"
  #define SECRET_PASS "MOT_DE_PASSE_DU_PARTAGE_DE_CONNEXION"
  #define WPA

#endif
