const axios = require('axios').default;

function getFakeData() {
  return axios.get("http://localhost:5000")
    .then(res => {
      console.log(res.data)
      return res.data;
    });
}

export default getFakeData;