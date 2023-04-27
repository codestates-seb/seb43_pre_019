import styled from 'styled-components';
import { useNavigate } from 'react-router-dom';
import { ReactComponent as Earth } from '../../assets/ic-earth.svg';
import { ReactComponent as Star } from '../../assets/start.svg';
import { ReactComponent as Team } from '../../assets/team_img.svg';
import ItemList from './ItemList';

const Container = styled.div`
  width: 164px;
  background-color: white;
  border-right: 1px solid lightgray;
`;

const InnerContainer = styled.div`
  position: sticky;
  top: 64px;
  font-size: 13px;
  color: #525960;
`;

const StyledButton = styled.button`
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  border: none;
  padding: 10px;
  background-color: white;
  color: #525960;
  text-align: left;

  &:active {
    padding: 10px 10px 10px 35px;
    background-color: #f1f2f3;
    font-weight: bold;
    border-right: 3px solid #f48024;
  }
`;

const StyledQButton = styled.button`
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  border: none;
  padding: 10px;
  background-color: white;
  color: #525960;
  text-align: left;

  &:active {
    padding: 10px;
    background-color: #f1f2f3;
    font-weight: bold;
    border-right: 3px solid #f48024;
    .earth_icon {
      path {
        fill: black;
      }
    }
  }

  :hover {
    color: black;
    fill: black;
  }
`;

const SideInnerBox = styled.div`
  border: 1px solid lightgray;
  padding: 10px;
  button {
    background-color: #f38225;
    color: white;
    border-radius: 5px;
    padding: 10px;
  }
`;

const WhyText = styled.div`
  padding: 6.6px;
  text-align: center;
`;

function Aside() {
  const navigate = useNavigate();
  return (
    <Container>
      <InnerContainer>
        <ItemList title="Home" />
        <ItemList title="PUBLIC">
          <StyledQButton onClick={() => navigate('/questions')}>
            <Earth className="earth_icon" /> Questions
          </StyledQButton>
          {/* {menus.map(menu => (
            <StyledButton className="pl">{menu}</StyledButton>
          ))} */}
          <StyledButton className="pl">Tags</StyledButton>
          <StyledButton className="pl" onClick={() => navigate('/mypages')}>
            Users
          </StyledButton>
          <StyledButton className="pl">Companies</StyledButton>
        </ItemList>
        <ItemList title="COLLECTIVES">
          <div>
            <Star className="star_icon" /> Explore Collectives
          </div>
        </ItemList>
        <ItemList title="TEAMS">
          <SideInnerBox>
            <div>
              <b>Stack Overflow for Teams</b> - Start collaborating and sharing
              organizational knowledge
            </div>
            <Team />
            <div>
              <button>Create a free Team</button>
              <WhyText>why Teams?</WhyText>
            </div>
          </SideInnerBox>
        </ItemList>
      </InnerContainer>
    </Container>
  );
}

export default Aside;
