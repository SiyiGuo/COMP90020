const axios = require('axios').default;

function getFakeData() {
  return axios.get("http://localhost:3000/fakedata/fakedata.json")
    .then(res => {
      return res.data;
    });
}

export default getFakeData;