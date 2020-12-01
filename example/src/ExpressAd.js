import React, {useEffect} from 'react';
import {StyleSheet, Text, View, TouchableOpacity} from 'react-native';
import {ad} from 'react-native-ad';

export default function ExpressAd() {
  useEffect(() => {
    ad.init({
      appid: '5112984',
    });
    return () => {};
  }, []);
  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>
        ☆ Express Ad example, Powered By Vevy ☆
      </Text>
      <TouchableOpacity
        style={{
          marginVertical: 20,
          paddingHorizontal: 30,
          paddingVertical: 15,
          backgroundColor: '#F96',
          borderRadius: 50,
        }}
        onPress={() => {
          const rewardVideo = ad.startExpress({
            appid: '5112984',
            codeid: '945652198',
          });
        }}>
        <Text style={{textAlign: 'center'}}> Start ExpressAd</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});
